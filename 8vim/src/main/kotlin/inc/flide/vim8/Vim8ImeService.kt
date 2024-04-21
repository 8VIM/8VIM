package inc.flide.vim8

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.EmbeddedMode
import inc.flide.vim8.ime.PopupMode
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.InputFeedbackController
import inc.flide.vim8.ime.input.LocalInputFeedbackController
import inc.flide.vim8.ime.keyboard.compose.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.compose.ProvideKeyboardHeight
import inc.flide.vim8.ime.keyboard.view.KeyboardLayout
import inc.flide.vim8.ime.keyboard.view.NumberLayout
import inc.flide.vim8.ime.keyboard.view.SelectionLayout
import inc.flide.vim8.ime.keyboard.view.SymbolsLayout
import inc.flide.vim8.ime.layout.loadKeyboardData
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.safeLoadKeyboardData
import inc.flide.vim8.ime.lifecycle.LifecycleInputMethodService
import inc.flide.vim8.ime.theme.ImeTheme
import inc.flide.vim8.lib.compose.ProvideLocalizedResources
import inc.flide.vim8.lib.compose.SystemUiIme
import inc.flide.vim8.lib.geometry.toIntOffset
import inc.flide.vim8.lib.util.InputMethodUtils
import java.lang.ref.WeakReference

private var vim8ImeServiceReference = WeakReference<Vim8ImeService?>(null)

class Vim8ImeService : LifecycleInputMethodService() {
    companion object {
        fun currentInputConnection(): InputConnection? =
            vim8ImeServiceReference.get()?.currentInputConnection

        fun inputFeedbackController(): InputFeedbackController? =
            vim8ImeServiceReference.get()?.inputFeedbackController

        fun switchToEmoticonKeyboard() {
            vim8ImeServiceReference.get()?.let { InputMethodUtils.switchToEmoticonKeyboard(it) }
        }

        fun hideKeyboard() {
            vim8ImeServiceReference.get()?.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun keyboardData() = vim8ImeServiceReference.get()?.keyboardData
    }

    private val prefs by appPreferenceModel()
    private val themeManager by themeManager()
    private val keyboardManager by keyboardManager()
    private val editorInstance by editorInstance()
    private val layoutLoader by layoutLoader()

    private var resourcesContext by mutableStateOf(this as Context)
    private var inputWindowView by mutableStateOf<View?>(null)
    private var isFullscreenUiMode by mutableStateOf(false)
    private val activeState get() = keyboardManager.activeState
    private val inputFeedbackController by lazy { InputFeedbackController.new(this) }
    var keyboardData: KeyboardData? by mutableStateOf(null)

    init {
        setTheme(R.style.AppTheme_Keyboard)
    }

    override fun onCreate() {
        super.onCreate()
        vim8ImeServiceReference = WeakReference(this)
        resourcesContext = createConfigurationContext(Configuration(resources.configuration))

        prefs.layout.current.observe {
            it.loadKeyboardData(layoutLoader, this)
                .onRight { keyboardData ->
                    this.keyboardData = keyboardData
                }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        themeManager.configuration = newConfig
        themeManager.updateCurrentTheme()
    }

    override fun onCreateInputView(): View {
        super.installViewTreeOwners()
        keyboardData = safeLoadKeyboardData(layoutLoader, this)
        val composeView = ComposeInputView()
        inputWindowView = composeView
        return composeView
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        val configuration = resources.configuration

        isFullscreenUiMode = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            configuration.screenHeightDp < 480

        return isFullscreenUiMode && (prefs.keyboard.fullScreenMode.get() is EmbeddedMode)
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        activeState.batchEdit {
            editorInstance.handleStartInputView(info)
        }
    }

    override fun onCreateCandidatesView(): View? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        vim8ImeServiceReference = WeakReference(null)
        inputWindowView = null
    }

    @Composable
    private fun ImeUiWrapper() {
        ProvideLocalizedResources(resourcesContext) {
            ProvideKeyboardHeight(isFullscreenUiMode) {
                val keyboardHeight = LocalKeyboardHeight.current
                CompositionLocalProvider(
                    LocalInputFeedbackController provides inputFeedbackController
                ) {
                    Surface(
                        modifier = Modifier.height(keyboardHeight),
                        color = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            ImeUi()
                        }
                        SystemUiIme()
                    }
                }
            }
        }
    }

    @Composable
    private fun ImeUi() {
        val state by keyboardManager.activeState.collectAsState()
        when (state.imeUiMode) {
            ImeUiMode.TEXT, ImeUiMode.CLIPBOARD -> KeyboardLayout()
            ImeUiMode.NUMERIC -> NumberLayout()
            ImeUiMode.SELECTION -> SelectionLayout()
            ImeUiMode.SYMBOLS -> SymbolsLayout()
        }
    }

    private inner class ComposeInputView : AbstractComposeView(this) {

        init {
            isHapticFeedbackEnabled = true
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        @Composable
        override fun Content() {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val fullScreenMode by prefs.keyboard.fullScreenMode.observeAsState()
            ImeTheme {
                if (isFullscreenUiMode && fullScreenMode is PopupMode) {
                    if (fullScreenMode.rect().size.isEmpty()) {
                        val height = configuration.screenHeightDp.toFloat() * 0.6f
                        val size = Size(
                            height * 1.5f.coerceAtMost(configuration.screenWidthDp.toFloat()),
                            height
                        )
                        fullScreenMode.update {
                            Rect(
                                offset =
                                Offset(
                                    configuration.screenWidthDp.toFloat(),
                                    height / 2f
                                ),
                                size = size
                            )
                        }.let {
                            prefs.keyboard.fullScreenMode.set(it)
                        }
                    }
                    val size = DpSize(
                        fullScreenMode.rect().size.width.dp,
                        fullScreenMode.rect().size.height.dp
                    )
                    var totalSize by remember { mutableStateOf(IntSize.Zero) }
                    var resizeOffset by remember { mutableStateOf<Offset?>(null) }

                    Popup(
                        popupPositionProvider = KeyboardPopupPositionProvider(
                            fullScreenMode.offset().toIntOffset(),
                            size,
                            density
                        ),
                        properties = PopupProperties(
                            dismissOnClickOutside = false
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .width(size.width)
                                .border(2.dp, Color.Red, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .onGloballyPositioned { totalSize = it.size }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                        if (down.type != PointerType.Touch) return@awaitEachGesture
                                        val cornerSize = 16.dp.roundToPx()
                                        val isLeft =
                                            down.position.x >= 0 && down.position.x <= cornerSize
                                        val isRight =
                                            down.position.x >= totalSize.width - cornerSize &&
                                                down.position.x <= totalSize.width
                                        val isTop =
                                            down.position.y >= 0 && down.position.y <= cornerSize
                                        val isBottom =
                                            down.position.y >= totalSize.height - cornerSize &&
                                                down.position.y <= totalSize.height
                                        if (
                                            (isLeft && isTop) ||
                                            (isLeft && isBottom) ||
                                            (isRight && isTop) ||
                                            (isRight && isBottom)
                                        ) {
                                            resizeOffset = down.position
                                            do {
                                                val event =
                                                    awaitPointerEvent(PointerEventPass.Initial)
                                                if (resizeOffset == null) continue
                                                val change = event.changes[0]
                                                val delta = change.position - resizeOffset!!
                                                resizeOffset = change.position
                                                val offset = fullScreenMode.offset()
                                                val currentSize = fullScreenMode.rect().size
                                                val newSize = Size(
                                                    (currentSize.width + delta.x).coerceAtMost(
                                                        configuration.screenWidthDp.toFloat()
                                                    ),
                                                    (currentSize.height + delta.y).coerceAtMost(
                                                        configuration.screenHeightDp.toFloat()
                                                    )
                                                )
                                                val newOffset = if (delta.x >= 0f &&
                                                    delta.y >= 0f
                                                ) {
                                                    offset
                                                } else {
                                                    val offsetX = (offset.x + delta.x).coerceIn(
                                                        0f,
                                                        configuration.screenWidthDp
                                                            .toFloat() - newSize.width
                                                    )
                                                    val offsetY = (offset.y + delta.y).coerceIn(
                                                        -configuration.screenHeightDp.toFloat(),
                                                        -newSize.height
                                                    )
                                                    Offset(offsetX, offsetY)
                                                }
                                                val r = Rect(newOffset, newSize)
                                                Log.d(
                                                    "fullscreen",
                                                    "$delta os: ${fullScreenMode.rect()}, ns: $r"
                                                )

                                                prefs.keyboard.fullScreenMode.set(
                                                    fullScreenMode.update {
                                                        Rect(newOffset, newSize)
                                                    }
                                                )
                                            } while (resizeOffset != null &&
                                                event.changes.any { it.id == down.id && it.pressed }
                                            )
                                            resizeOffset = null
                                        } else {
                                            resizeOffset = null
                                        }
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(fullScreenMode.rect().size.width.dp)
                                    .height(fullScreenMode.rect().size.height.dp)
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            val down =
                                                awaitFirstDown(pass = PointerEventPass.Initial)
                                            if (down.type == PointerType.Touch) {
                                                resizeOffset = null
                                            }
                                        }
                                    }
                            ) {
                                ImeUiWrapper()
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, amount ->
                                            change.consume()
                                            fullScreenMode
                                                .update {
                                                    Rect(
                                                        Offset(
                                                            it.left + amount.x,
                                                            it.top + amount.y
                                                        ),
                                                        it.size
                                                    )
                                                }
                                                .let { prefs.keyboard.fullScreenMode.set(it) }
                                        }
                                    },
                                thickness = 5.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else {
                    ImeUiWrapper()
                }
            }
        }

        override fun getAccessibilityClassName(): CharSequence {
            return javaClass.name
        }
    }

    private class KeyboardPopupPositionProvider(
        val offset: IntOffset,
        val size: DpSize,
        val density: Density
    ) :
        PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset {
            return IntOffset(
                offset.x.coerceIn(0, windowSize.width - with(density) { size.width.roundToPx() }),
                offset.y.coerceIn(-windowSize.height, -with(density) { size.height.roundToPx() })
            )
        }
    }
}
