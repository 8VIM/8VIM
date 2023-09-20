package inc.flide.vim8

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.color.DynamicColors
import inc.flide.vim8.ime.KeyboardTheme
import inc.flide.vim8.ime.KeyboardTheme.Companion.getInstance
import inc.flide.vim8.ime.services.ClipboardManagerService
import inc.flide.vim8.ime.services.ClipboardManagerService.ClipboardHistoryListener
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API28_P
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.models.safeLoadKeyboardData
import inc.flide.vim8.views.ClipboardKeypadView
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import inc.flide.vim8.views.mainkeyboard.MainKeyboardView

class MainInputMethodService : InputMethodService(), ClipboardHistoryListener {
    private val prefs by appPreferenceModel()
    private var inputConnection: InputConnection? = null
    private lateinit var clipboardKeypadView: ClipboardKeypadView
    private lateinit var editorInfo: EditorInfo
    lateinit var clipboardManagerService: ClipboardManagerService
        private set
    private lateinit var currentKeypadView: View
    private lateinit var mainKeyboardView: MainKeyboardView
    private lateinit var numberKeypadView: NumberKeypadView
    private lateinit var selectionKeypadView: SelectionKeypadView
    private lateinit var symbolKeypadView: SymbolKeypadView
    var shiftLockFlag = 0
        set(shiftLockFlag) {
            field = shiftLockFlag
            if (window.findViewById<View?>(R.id.xboardView) != null) {
                window.findViewById<View>(R.id.xboardView).invalidate()
            }
        }
    var capsLockFlag = 0
    private var modifierFlags = 0
    private lateinit var keyboardTheme: KeyboardTheme

    init {
        setTheme(R.style.AppTheme_NoActionBar)
    }

    private fun setCurrentKeypadView(view: View) {
        currentKeypadView = view
        currentKeypadView.invalidate()
        setInputView(currentKeypadView)
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this.application)
        val applicationContext = applicationContext
        clipboardManagerService = ClipboardManagerService(applicationContext)
        clipboardManagerService.setClipboardHistoryListener(this)
        keyboardTheme = getInstance()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        keyboardTheme.configuration = newConfig
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
        numberKeypadView = NumberKeypadView(this)
        selectionKeypadView = SelectionKeypadView(this)
        clipboardKeypadView = ClipboardKeypadView(this)
        symbolKeypadView = SymbolKeypadView(this)
        mainKeyboardView = MainKeyboardView(this)
        setCurrentKeypadView(mainKeyboardView)

        if (ATLEAST_API28_P) {
            window.window?.let {
                val windowInsetsControllerCompat = WindowInsetsControllerCompat(
                    it,
                    it.decorView
                )
                keyboardTheme.onChange {
                    setNavigationBarColor(
                        it,
                        windowInsetsControllerCompat
                    )
                }
                setNavigationBarColor(it, windowInsetsControllerCompat)
            }
        }

        return currentKeypadView
    }

    private fun setNavigationBarColor(
        window: Window,
        windowInsetsControllerCompat: WindowInsetsControllerCompat
    ) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = keyboardTheme.backgroundColor
        val isLight = ColorUtils.calculateLuminance(
            keyboardTheme.backgroundColor
        ) >= 0.5
        windowInsetsControllerCompat.isAppearanceLightNavigationBars = isLight
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputConnection = currentInputConnection
    }

    override fun onBindInput() {
        inputConnection = currentInputConnection
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        val configuration = resources.configuration
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            configuration.screenHeightDp < 480
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info
        when (editorInfo.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> switchToNumberPad()

            InputType.TYPE_CLASS_TEXT -> switchToMainKeypad()
            else -> switchToMainKeypad()
        }
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        inputConnection = currentInputConnection
        shiftLockFlag = 0
        capsLockFlag = 0
        clearModifierFlags()
    }

    fun loadKeyboardData(): KeyboardData {
        return prefs.layout.current.get().safeLoadKeyboardData(applicationContext)
    }

    fun sendText(text: String?) {
        inputConnection?.commitText(text, 1)
        clearModifierFlags()
    }

    private fun clearModifierFlags() {
        modifierFlags = 0
    }

    fun sendDownKeyEvent(keyEventCode: Int, flags: Int) {
        inputConnection?.sendKeyEvent(
            KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                keyEventCode,
                0,
                flags
            )
        )
    }

    fun sendUpKeyEvent(keyEventCode: Int, flags: Int) {
        inputConnection?.sendKeyEvent(
            KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                keyEventCode,
                0,
                flags
            )
        )
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

            // Before returning verify that this keyboard Id we have does exist in the
            // system.
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return inputMethodManager.enabledInputMethodList
                .find { it.id == emoticonKeyboardId }?.let { emoticonKeyboardId }.orEmpty()
        }

    fun sendKey(keyEventCode: Int, flags: Int) {
        sendDownAndUpKeyEvent(keyEventCode, shiftLockFlag or capsLockFlag or modifierFlags or flags)
        clearModifierFlags()
    }

    fun delete() {
        inputConnection?.getSelectedText(0)?.let {
            if (TextUtils.isEmpty(it)) {
                inputConnection?.deleteSurroundingTextInCodePoints(1, 0)
            } else {
                inputConnection?.commitText("", 0)
            }
        }
    }

    fun switchAnchor() {
        inputConnection?.getExtractedText(
            ExtractedTextRequest(),
            InputConnection.GET_EXTRACTED_TEXT_MONITOR
        )?.let {
            val start = it.selectionStart
            val end = it.selectionEnd
            inputConnection?.setSelection(end, start)
        }
    }

    fun switchToSelectionKeypad() {
        setCurrentKeypadView(selectionKeypadView)
    }

    fun switchToClipboardKeypad() {
        setCurrentKeypadView(clipboardKeypadView)
    }

    fun switchToSymbolsKeypad() {
        setCurrentKeypadView(symbolKeypadView)
    }

    fun switchToMainKeypad() {
        setCurrentKeypadView(mainKeyboardView)
    }

    fun switchToNumberPad() {
        setCurrentKeypadView(numberKeypadView)
    }

    fun cut() {
        inputConnection?.performContextMenuAction(android.R.id.cut)
    }

    fun copy() {
        inputConnection?.performContextMenuAction(android.R.id.copy)
    }

    fun paste() {
        inputConnection?.performContextMenuAction(android.R.id.paste)
    }

    fun hideKeyboard() {
        requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun performShiftToggle() {
        // single press locks the shift key,
        // double press locks the caps key
        // a third press unlocks both.
        if (shiftLockFlag == KeyEvent.META_SHIFT_ON) {
            shiftLockFlag = 0
            capsLockFlag = KeyEvent.META_CAPS_LOCK_ON
        } else if (capsLockFlag == KeyEvent.META_CAPS_LOCK_ON) {
            shiftLockFlag = 0
            capsLockFlag = 0
        } else {
            shiftLockFlag = KeyEvent.META_SHIFT_ON
            capsLockFlag = 0
        }
    }

    fun areCharactersCapitalized(): Boolean {
        return shiftLockFlag == KeyEvent.META_SHIFT_ON || capsLockFlag == KeyEvent.META_CAPS_LOCK_ON
    }

    /*
     * |-------|-------|-------|-------|
     * 1111 IME_MASK_ACTION
     * |-------|-------|-------|-------|
     * IME_ACTION_UNSPECIFIED
     * 1 IME_ACTION_NONE
     * 1 IME_ACTION_GO
     * 11 IME_ACTION_SEARCH
     * 1 IME_ACTION_SEND
     * 1 1 IME_ACTION_NEXT
     * 11 IME_ACTION_DONE
     * 111 IME_ACTION_PREVIOUS
     * 1 IME_FLAG_NO_PERSONALIZED_LEARNING
     * 1 IME_FLAG_NO_FULLSCREEN
     * 1 IME_FLAG_NAVIGATE_PREVIOUS
     * 1 IME_FLAG_NAVIGATE_NEXT
     * 1 IME_FLAG_NO_EXTRACT_UI
     * 1 IME_FLAG_NO_ACCESSORY_ACTION
     * 1 IME_FLAG_NO_ENTER_ACTION
     * 1 IME_FLAG_FORCE_ASCII
     * |-------|-------|-------|-------|
     */
    fun commitImeOptionsBasedEnter() {
        when (val imeAction = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_PREVIOUS -> {
                val imeNoEnterFlag = editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION
                if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
                } else {
                    inputConnection?.performEditorAction(imeAction)
                }
            }

            EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE -> sendDownAndUpKeyEvent(
                KeyEvent.KEYCODE_ENTER,
                0
            )

            else -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
        }
    }

    override fun onClipboardHistoryChanged() {
        clipboardKeypadView.updateClipHistory()
    }
}
