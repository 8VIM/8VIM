package inc.flide.vim8

import android.content.Context
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import com.google.android.material.color.DynamicColors
import inc.flide.vim8.ime.KeyboardTheme
import inc.flide.vim8.ime.KeyboardTheme.Companion.getInstance
import inc.flide.vim8.ime.LayoutLoader
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.keyboard.KeyboardLayout
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.ProvideKeyboardHeight
import inc.flide.vim8.ime.lifecycle.LifecycleInputMethodService
import inc.flide.vim8.ime.nlp.BreakIteratorGroup
import inc.flide.vim8.ime.services.ClipboardManagerService.ClipboardHistoryListener
import inc.flide.vim8.ime.theme.ImeTheme
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API28_P
import inc.flide.vim8.lib.compose.ProvideLocalizedResources
import inc.flide.vim8.lib.compose.SystemUiIme
import inc.flide.vim8.lib.util.InputMethodUtils
import java.lang.ref.WeakReference

private var Vim8ImeServiceReference = WeakReference<Vim8ImeService?>(null)

class Vim8ImeService : LifecycleInputMethodService(), ClipboardHistoryListener {
    companion object {
        fun currentInputConnection(): InputConnection? {
            return Vim8ImeServiceReference.get()?.currentInputConnection
        }
        fun switchToEmoticonKeyboard() {
            Vim8ImeServiceReference.get()?.let { InputMethodUtils.switchToEmoticonKeyboard(it) }
        }
    }

    private val prefs by appPreferenceModel()
    private val themeManager by themeManager()
    private val keyboardManager by keyboardManager()
    private val editorInstance by editorInstance()

    private var resourcesContext by mutableStateOf(this as Context)
    private var inputWindowView by mutableStateOf<View?>(null)
    private val activeState get() = keyboardManager.activeState


    //    private lateinit var currentKeypadView: View
//    private lateinit var mainKeyboardView: MainKeyboardView
//    private lateinit var numberKeypadView: NumberKeypadView
//    private lateinit var selectionKeypadView: SelectionKeypadView
//    private lateinit var symbolKeypadView: SymbolKeypadView
    private lateinit var breakIteratorGroup: BreakIteratorGroup
    private lateinit var layoutLoader: LayoutLoader

    var isPassword = false
        private set
    var ctrlState = false
        private set
    val ctrlFlag: Int
        get() = if (!ctrlState) {
            0
        } else {
            KeyEvent.META_CTRL_MASK
        }
    var shiftState = State.OFF
        set(value) {
            field = value
            updateShiftButton()
        }
    val shiftLockFlag: Int
        get() = if (shiftState == State.ON) KeyEvent.META_SHIFT_ON else 0
    val capsLockFlag: Int
        get() = if (shiftState == State.ENGAGED) KeyEvent.META_CAPS_LOCK_ON else 0
    private lateinit var keyboardTheme: KeyboardTheme

    init {
        setTheme(R.style.AppTheme_Keyboard)
    }

    private fun setCurrentKeypadView(view: View) {
//        currentKeypadView = view
//        currentKeypadView.invalidate()
//        setInputView(currentKeypadView)
    }

    override fun onCreate() {
        super.onCreate()
        Vim8ImeServiceReference = WeakReference(this)
        resourcesContext = createConfigurationContext(Configuration(resources.configuration))
        layoutLoader = applicationContext.layoutLoader().value
        breakIteratorGroup = BreakIteratorGroup(applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(application)
//        clipboardManagerService = ClipboardManagerService(applicationContext)
//        clipboardManagerService.setClipboardHistoryListener(this)
        keyboardTheme = getInstance()
        val moveByWord = prefs.keyboard.behavior.cursor.moveByWord
        ctrlState = moveByWord.get()
        moveByWord.observe {
            if (it != ctrlState) {
                ctrlState = it
                updateCtrlButton()
            }
        }
    }

    private fun updateCtrlButton() {
//        ctrlButtonViews.forEach {
//            it.updateCtrlButton()
//            (it as View).invalidate()
//        }
    }

    private fun updateShiftButton() {
//        shiftButtonViews.forEach {
//            it.updateShiftButton()
//            (it as View).invalidate()
//        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        themeManager.configuration = newConfig
        themeManager.updateCurrentTheme()
    }

    /**
     * Lifecycle of IME
     *
     *
     * 01. InputMethodService Starts
     * 02. onCreate()
     * 03. onCreateInputView()
     * 04. onCreateCandidateViews()
     * 05. onStartInputViews()
     * 06. Text input gets the current input method subtype
     * 07. InputMethodManager#getCurrentInputMethodSubtype()
     * 08. Text input has started
     * 09. onCurrentInputMethodSubtypeChanged()
     * 10. Detect the current input method subtype has been changed -> can go to
     * step 6
     * 11. onFinishInput() -> cursor can Move to an additional field -> step 5
     * 12. onDestroy()
     * 13. InputMethodService stops
     */
    override fun onCreateInputView(): View {
        super.installViewTreeOwners()
        val composeView = ComposeInputView()
        inputWindowView = composeView
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
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

    fun sendText(text: String?) {
//        inputConnection?.commitText(text, 1)
    }

    fun sendDownKeyEvent(keyEventCode: Int, flags: Int) {
//        inputConnection?.sendKeyEvent(
//            KeyEvent(
//                SystemClock.uptimeMillis(),
//                SystemClock.uptimeMillis(),
//                KeyEvent.ACTION_DOWN,
//                keyEventCode,
//                0,
//                flags
//            )
//        )
    }

    fun sendUpKeyEvent(keyEventCode: Int, flags: Int) {
//        inputConnection?.sendKeyEvent(
//            KeyEvent(
//                SystemClock.uptimeMillis(),
//                SystemClock.uptimeMillis(),
//                KeyEvent.ACTION_UP,
//                keyEventCode,
//                0,
//                flags
//            )
//        )
    }

    fun sendDownAndUpKeyEvent(keyEventCode: Int, flags: Int) {
        sendDownKeyEvent(keyEventCode, flags)
        sendUpKeyEvent(keyEventCode, flags)
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

    fun sendKey(keyEventCode: Int, flags: Int) {
        sendDownAndUpKeyEvent(keyEventCode, flags)
    }

    fun resetShiftState() {
        if (shiftState == State.ON) {
            shiftState = State.OFF
            updateShiftButton()
        }
    }


    fun switchAnchor() {
        /*    inputConnection?.getExtractedText(
                ExtractedTextRequest(),
                InputConnection.GET_EXTRACTED_TEXT_MONITOR
            )?.let {
                val start = it.selectionStart
                val end = it.selectionEnd
                inputConnection?.setSelection(end, start)
            }*/
    }

    fun switchToSelectionKeypad() {
//        setCurrentKeypadView(selectionKeypadView)
    }

    fun switchToClipboardKeypad() {
//        clipboardKeypadView?.let { setCurrentKeypadView(it) }
    }

    fun switchToSymbolsKeypad() {
//        setCurrentKeypadView(symbolKeypadView)
    }

    fun switchToMainKeypad() {
//        setCurrentKeypadView(mainKeyboardView)
//        mainKeyboardView.setupClipboardButton()
    }

    fun switchToNumberPad() {
//        setCurrentKeypadView(numberKeypadView)
    }

    fun cut() {
//        inputConnection?.performContextMenuAction(android.R.id.cut)
    }

    fun copy() {
//        inputConnection?.performContextMenuAction(android.R.id.copy)
    }

    fun paste() {
//        inputConnection?.performContextMenuAction(android.R.id.paste)
    }

    fun hideKeyboard() {
        requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun performShiftToggle() {
        shiftState = when (shiftState) {
            State.OFF -> State.ON
            State.ON -> State.ENGAGED
            else -> State.OFF
        }
        updateShiftButton()
    }

    fun performCtrlToggle() {
        ctrlState = !ctrlState
        updateCtrlButton()
    }

    fun areCharactersCapitalized(): Boolean {
        return shiftState != State.OFF
    }

    override fun onCreateCandidatesView(): View? {
        // Disable the default candidates view
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Vim8ImeServiceReference = WeakReference(null)
        inputWindowView = null
    }

    fun commitImeOptionsBasedEnter() {
//        when (val imeAction = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
//            EditorInfo.IME_ACTION_GO,
//            EditorInfo.IME_ACTION_SEARCH,
//            EditorInfo.IME_ACTION_SEND,
//            EditorInfo.IME_ACTION_NEXT,
//            EditorInfo.IME_ACTION_DONE,
//            EditorInfo.IME_ACTION_PREVIOUS -> {
//                val imeNoEnterFlag = editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION
//                if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
//                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
//                } else {
//                    inputConnection?.performEditorAction(imeAction)
//                }
//            }
//
//            EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE -> sendDownAndUpKeyEvent(
//                KeyEvent.KEYCODE_ENTER,
//                0
//            )
//
//            else -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
//        }
    }

    override fun onClipboardHistoryChanged() {
//        clipboardKeypadView?.updateClipHistory()
    }

    enum class State {
        OFF, ON, ENGAGED
    }

    @Composable
    private fun ImeUiWrapper() {
        ProvideLocalizedResources(resourcesContext) {
            ProvideKeyboardHeight {
//                CompositionLocalProvider(LocalInputFeedbackController provides inputFeedbackController) {

                val keyboardHeight = LocalKeyboardHeight.current
                ImeTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyboardHeight)
                    ) {
                        ImeUi()
                    }
                    SystemUiIme()
                }
            }
//            }
        }
    }

    @Composable
    private fun ImeUi() {
        val state by keyboardManager.activeState.collectAsState()
        when (state.imeUiMode) {
            ImeUiMode.TEXT, ImeUiMode.CLIPBOARD -> KeyboardLayout()
            ImeUiMode.NUMERIC -> KeyboardLayout()
            ImeUiMode.SELECTION -> KeyboardLayout()
            ImeUiMode.SYMBOLS -> KeyboardLayout()
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
