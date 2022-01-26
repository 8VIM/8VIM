package inc.flide.vim8

import android.os.SystemClock
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper
import inc.flide.vim8.keyboardHelpers.KeyboardDataStore
import inc.flide.vim8.keyboardHelpers.LifecycleInputMethodService
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView
import inc.flide.vim8.views.mainKeyboard.XpadView
import java.lang.ref.WeakReference

private var MainInputMethodServiceReference = WeakReference<MainInputMethodService>(null)

class MainInputMethodService : LifecycleInputMethodService() {
    companion object {
        private var shiftLockFlag = 0
        private var capsLockFlag = 0
        private var modifierFlags = 0

        fun sendDownKeyEvent(keyEventCode: Int, flags: Int) {
            MainInputMethodServiceReference.get()!!.inputConnection.sendKeyEvent(
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
            MainInputMethodServiceReference.get()!!.inputConnection.sendKeyEvent(
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

        fun sendKey(keyEventCode: Int, flags: Int) {
            sendDownAndUpKeyEvent(keyEventCode, getShiftLockFlag() or getCapsLockFlag() or modifierFlags or flags)
            clearModifierFlags()
        }

        fun delete() {
            val sel = MainInputMethodServiceReference.get()!!.inputConnection.getSelectedText(0)
            if (TextUtils.isEmpty(sel)) {
                MainInputMethodServiceReference.get()!!.inputConnection.deleteSurroundingText(1, 0)
            } else {
                MainInputMethodServiceReference.get()!!.inputConnection.commitText("", 0)
            }
        }

        fun getShiftLockFlag(): Int {
            return shiftLockFlag
        }

        fun setShiftLockFlag(shiftLockFlag: Int) {
            this.shiftLockFlag = shiftLockFlag
            if (MainInputMethodServiceReference.get()!!.window.findViewById<View?>(R.id.xboardView) != null) {
                MainInputMethodServiceReference.get()!!.window.findViewById<View?>(R.id.xboardView).invalidate()
            }
        }

        fun getCapsLockFlag(): Int {
            return capsLockFlag
        }

        private fun setCapsLockFlag(capsLockFlag: Int) {
            this.capsLockFlag = capsLockFlag
        }

        private fun clearModifierFlags() {
            modifierFlags = 0
        }

        fun setModifierFlags(newModifierFlags: Int) {
            modifierFlags = modifierFlags or newModifierFlags
        }

        fun cut() {
            MainInputMethodServiceReference.get()!!.inputConnection.performContextMenuAction(android.R.id.cut)
        }

        fun copy() {
            MainInputMethodServiceReference.get()!!.inputConnection.performContextMenuAction(android.R.id.copy)
        }

        fun paste() {
            MainInputMethodServiceReference.get()!!.inputConnection.performContextMenuAction(android.R.id.paste)
        }

        fun areCharactersCapitalized(): Boolean {
            return getShiftLockFlag() == KeyEvent.META_SHIFT_ON || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON
        }

        fun sendText(text: String?) {
            MainInputMethodServiceReference.get()!!.inputConnection.commitText(text, 1)
            clearModifierFlags()
        }

        fun switchToExternalEmoticonKeyboard() {
            val inputMethodManager = MainInputMethodServiceReference.get()!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val iBinder = MainInputMethodServiceReference.get()!!.window.window!!.attributes.token
            val keyboardId = MainInputMethodServiceReference.get()!!.getSelectedEmoticonKeyboardId()
            if (keyboardId.isEmpty()) {
                inputMethodManager.switchToLastInputMethod(iBinder)
            } else {
                inputMethodManager.setInputMethod(iBinder, keyboardId)
            }
        }

        fun switchAnchor() {
            val extractedText = MainInputMethodServiceReference.get()!!.inputConnection.getExtractedText(ExtractedTextRequest(), InputConnection.GET_EXTRACTED_TEXT_MONITOR)
            val start = extractedText.selectionStart
            val end = extractedText.selectionEnd
            MainInputMethodServiceReference.get()!!.inputConnection.setSelection(end, start)
        }


        fun hideKeyboard() {
            MainInputMethodServiceReference.get()!!.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun performShiftToggle() {
            //single press locks the shift key,
            //double press locks the caps key
            //a third press unlocks both.
            when {
                getShiftLockFlag() == KeyEvent.META_SHIFT_ON -> {
                    setShiftLockFlag(0)
                    setCapsLockFlag(KeyEvent.META_CAPS_LOCK_ON)
                }
                getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON -> {
                    setShiftLockFlag(0)
                    setCapsLockFlag(0)
                }
                else -> {
                    setShiftLockFlag(KeyEvent.META_SHIFT_ON)
                    setCapsLockFlag(0)
                }
            }
        }

        fun commitImeOptionsBasedEnter() {
            /*
             * |-------|-------|-------|-------|
                     *                              1111 IME_MASK_ACTION
             * |-------|-------|-------|-------|
             *                                   IME_ACTION_UNSPECIFIED
             *                                 1 IME_ACTION_NONE
             *                                1  IME_ACTION_GO
             *                                11 IME_ACTION_SEARCH
             *                               1   IME_ACTION_SEND
             *                               1 1 IME_ACTION_NEXT
             *                               11  IME_ACTION_DONE
             *                               111 IME_ACTION_PREVIOUS
             *         1                         IME_FLAG_NO_PERSONALIZED_LEARNING
             *        1                          IME_FLAG_NO_FULLSCREEN
             *       1                           IME_FLAG_NAVIGATE_PREVIOUS
             *      1                            IME_FLAG_NAVIGATE_NEXT
             *     1                             IME_FLAG_NO_EXTRACT_UI
             *    1                              IME_FLAG_NO_ACCESSORY_ACTION
             *   1                               IME_FLAG_NO_ENTER_ACTION
             *  1                                IME_FLAG_FORCE_ASCII
             * |-------|-------|-------|-------|
             */
            when (val imeAction = MainInputMethodServiceReference.get()!!.editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_PREVIOUS -> {
                    val imeNoEnterFlag = MainInputMethodServiceReference.get()!!.editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION
                    if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                        sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
                    } else {
                        MainInputMethodServiceReference.get()!!.inputConnection.performEditorAction(imeAction)
                    }
                }
                EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
                else -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
            }
        }

        fun switchToSelectionKeypad() {
            //MainInputMethodServiceReference.get()!!.setCurrentKeypadView(MainInputMethodServiceReference.get()!!.selectionKeypadView)
        }

        fun switchToSymbolsKeypad() {
            //MainInputMethodServiceReference.get()!!.setCurrentKeypadView(MainInputMethodServiceReference.get()!!.symbolKeypadView)
        }

        fun switchToMainKeypad() {
            //MainInputMethodServiceReference.get()!!.setCurrentKeypadView(MainInputMethodServiceReference.get()!!.mainKeyboardView)
        }

        fun switchToNumberPad() {
            //MainInputMethodServiceReference.get()!!.setCurrentKeypadView(MainInputMethodServiceReference.get()!!.numberKeypadView)
        }
    }
    private lateinit var inputConnection: InputConnection
    private var inputWindowView by mutableStateOf<View?>(null)
    private lateinit var editorInfo: EditorInfo
    private lateinit var mainKeyboardView: MainKeyboardView
    private lateinit var numberKeypadView: NumberKeypadView
    private lateinit var selectionKeypadView: SelectionKeypadView
    private lateinit var symbolKeypadView: SymbolKeypadView
    private lateinit var currentKeypadView: View
    private fun setCurrentKeypadView(view: View) {
        currentKeypadView = view
        currentKeypadView.invalidate()
        setInputView(currentKeypadView)
    }

    /**
     * Lifecycle of IME
     *
     *
     * 01.  InputMethodService Starts
     * 02.  onCreate()
     * 03.  onCreateInputView()
     * 04.  onCreateCandidateViews()
     * 05.  onStartInputViews()
     * 06.  Text input gets the current input method subtype
     * 07.  InputMethodManager#getCurrentInputMethodSubtype()
     * 08.  Text input has started
     * 09.  onCurrentInputMethodSubtypeChanged()
     * 10. Detect the current input method subtype has been changed -> can go to step 6
     * 11. onFinishInput() -> cursor can Move to an additional field -> step 5
     * 12. onDestroy()
     * 13. InputMethodService stops
     */

    override fun onCreate() {
        super.onCreate()
        MainInputMethodServiceReference = WeakReference(this)
        KeyboardDataStore.keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, applicationContext)
    }
    override fun onCreateInputView(): View {
        super.onCreateInputView()
        val composeView = ComposeInputView()
        inputWindowView = composeView
        numberKeypadView = NumberKeypadView(this)
        selectionKeypadView = SelectionKeypadView(this)
        symbolKeypadView = SymbolKeypadView(this)
        mainKeyboardView = MainKeyboardView(this)
        //setCurrentKeypadView(mainKeyboardView)
        //return currentKeypadView
        return composeView
    }

    @Composable
    private fun ImeUiWrapper() {
        // Outer box is necessary as an "outer window"
        Box(modifier = Modifier.height(250.dp)) {
            //Text(text = "Hodor Hodor!", fontSize = 50.sp, modifier = Modifier.align(Alignment.Center))
            xPad()
        }
    }

    @Composable
    fun xPad() {
        Canvas(
            modifier = Modifier
                .size(330.dp, 250.dp)
                .background(color = Color.White),
            onDraw =  {
                val userPrefersTypingTrail: Boolean = SharedPreferenceHelper.getInstance(applicationContext)
                    .getBoolean(
                        applicationContext.getString(R.string.pref_typing_trail_visibility_key),
                        true)
                if (userPrefersTypingTrail) {
                    //paintTypingTrail(canvas)
                }
                drawCircle(
                    color = Color.Black,
                    center = center,
                    radius = 50f
                )
                //drawPath(color = Color(XpadView.foregroundColor), path = XpadView.sectorLines)
            }
        )
    }

    override fun onCreateCandidatesView(): View? {
        // Disable the default candidates view
        return null
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info
        when (editorInfo.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME -> switchToNumberPad()
            InputType.TYPE_CLASS_TEXT -> switchToMainKeypad()
            else -> switchToMainKeypad()
        }
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputConnection = currentInputConnection
    }

    override fun onBindInput() {
        inputConnection = currentInputConnection
    }

    override fun onDestroy() {
        super.onDestroy()
        MainInputMethodServiceReference = WeakReference(null)
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        inputConnection = currentInputConnection
        setShiftLockFlag(0)
        setCapsLockFlag(0)
        clearModifierFlags()
    }

    private fun getSelectedEmoticonKeyboardId(): String {
        val emoticonKeyboardId: String = SharedPreferenceHelper.getInstance(applicationContext)
            .getString(getString(R.string.pref_selected_emoticon_keyboard), "")

        // Before returning verify that this keyboard Id we have does exist in the system.
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        for (inputMethodInfo in enabledInputMethodList) {
            if (inputMethodInfo.id.compareTo(emoticonKeyboardId) == 0) {
                return emoticonKeyboardId
            }
        }
        return ""
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