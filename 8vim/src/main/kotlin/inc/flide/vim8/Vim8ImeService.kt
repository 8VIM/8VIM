package inc.flide.vim8

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
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
import inc.flide.vim8.ime.ui.ImeLayout
import inc.flide.vim8.ime.ui.KeyboardLayoutMode
import inc.flide.vim8.lib.android.isFullScreen
import inc.flide.vim8.lib.android.isTablet
import inc.flide.vim8.lib.compose.ProvideLocalizedResources
import inc.flide.vim8.lib.compose.SystemUiIme
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
    private var isFloating by mutableStateOf(false)
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
        isFloating = prefs.keyboard.layoutMode.mode.get() == KeyboardLayoutMode.FLOATING

        prefs.keyboard.layoutMode.mode.observe {
            isFloating = it == KeyboardLayoutMode.FLOATING
        }

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
        if ((configuration.isFullScreen() || configuration.isTablet()) &&
            prefs.keyboard.layoutMode.mode.getOrNull() == null
        ) {
            prefs.keyboard.layoutMode.mode.set(KeyboardLayoutMode.FLOATING)
        }
        return super.onEvaluateFullscreenMode()
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
            ProvideKeyboardHeight {
                val keyboardHeight = LocalKeyboardHeight.current
                CompositionLocalProvider(
                    LocalInputFeedbackController provides inputFeedbackController
                ) {
                    Surface(
                        modifier = Modifier.height(keyboardHeight),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
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
            ImeLayout(isFloating) {
                ImeUiWrapper()
            }
        }

        override fun getAccessibilityClassName(): CharSequence {
            return javaClass.name
        }
    }
}
