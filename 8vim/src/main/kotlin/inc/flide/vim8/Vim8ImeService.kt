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
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.ProvideKeyboardHeight
import inc.flide.vim8.ime.layout.loadKeyboardData
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.safeLoadKeyboardData
import inc.flide.vim8.ime.lifecycle.LifecycleInputMethodService
import inc.flide.vim8.ime.theme.ImeTheme
import inc.flide.vim8.ime.views.KeyboardLayout
import inc.flide.vim8.ime.views.NumberLayout
import inc.flide.vim8.ime.views.SelectionLayout
import inc.flide.vim8.ime.views.SymbolsLayout
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API28_P
import inc.flide.vim8.lib.compose.ProvideLocalizedResources
import inc.flide.vim8.lib.compose.SystemUiIme
import inc.flide.vim8.lib.util.InputMethodUtils
import java.lang.ref.WeakReference

private var Vim8ImeServiceReference = WeakReference<Vim8ImeService?>(null)

class Vim8ImeService : LifecycleInputMethodService() {
    companion object {
        fun currentInputConnection(): InputConnection? =
            Vim8ImeServiceReference.get()?.currentInputConnection

        fun inputFeedbackController(): InputFeedbackController? =
            Vim8ImeServiceReference.get()?.inputFeedbackController

        fun switchToEmoticonKeyboard() {
            Vim8ImeServiceReference.get()?.let { InputMethodUtils.switchToEmoticonKeyboard(it) }
        }

        fun hideKeyboard() {
            Vim8ImeServiceReference.get()?.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun keyboardData() = Vim8ImeServiceReference.get()?.keyboardData
    }

    private val prefs by appPreferenceModel()
    private val themeManager by themeManager()
    private val keyboardManager by keyboardManager()
    private val editorInstance by editorInstance()
    private val layoutLoader by layoutLoader()

    private var resourcesContext by mutableStateOf(this as Context)
    private var inputWindowView by mutableStateOf<View?>(null)
    private val activeState get() = keyboardManager.activeState
    private val inputFeedbackController by lazy { InputFeedbackController.new(this) }
    var keyboardData: KeyboardData? by mutableStateOf(null)

    init {
        setTheme(R.style.AppTheme_Keyboard)
    }

    override fun onCreate() {
        super.onCreate()
        Vim8ImeServiceReference = WeakReference(this)
        resourcesContext = createConfigurationContext(Configuration(resources.configuration))
        keyboardData = safeLoadKeyboardData(layoutLoader, this)!!
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
        val composeView = ComposeInputView()
        inputWindowView = composeView
        return composeView
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        val configuration = resources.configuration
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            configuration.screenHeightDp < 480
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        activeState.batchEdit {
            editorInstance.handleStartInputView(info)
        }
    }

    @Suppress("DEPRECATION")
    fun switchToExternalEmoticonKeyboard() {
        val keyboardId = selectedEmoticonKeyboardId
        if (keyboardId.isEmpty()) {
            if (ATLEAST_API28_P) {
                switchToPreviousInputMethod()
            } else {
                val inputMethodManager = this
                    .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                val tokenIBinder = window.window!!.attributes.token
                inputMethodManager.switchToLastInputMethod(tokenIBinder)
            }
        } else {
            switchInputMethod(keyboardId)
        }
    }

    private val selectedEmoticonKeyboardId: String
        get() {
            val emoticonKeyboardId = prefs.keyboard.emoticonKeyboard.get()

            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return inputMethodManager.enabledInputMethodList
                .find { it.id == emoticonKeyboardId }?.let { emoticonKeyboardId }.orEmpty()
        }

    override fun onCreateCandidatesView(): View? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Vim8ImeServiceReference = WeakReference(null)
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
                    ImeTheme {
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
            ImeUiWrapper()
        }

        override fun getAccessibilityClassName(): CharSequence {
            return javaClass.name
        }
    }
}
