package inc.flide.vim8

import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Bundle
import inc.flide.vim8.R
import android.view.View.OnTouchListener
import android.content.Intent
import inc.flide.vim8.ui.SettingsActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import inc.flide.vim8.ui.SettingsFragment
import android.widget.Toast
import inc.flide.vim8.ui.AboutUsActivity
import androidx.core.view.GravityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.afollestad.materialdialogs.DialogAction
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences
import android.app.Activity
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import androidx.preference.SeekBarPreference
import inc.flide.vim8.preferences.SharedPreferenceHelper
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
import inc.flide.vim8.R.raw
import inc.flide.vim8.structures.LayoutFileName
import android.graphics.PointF
import inc.flide.vim8.geometry.Circle
import android.graphics.RectF
import android.graphics.PathMeasure
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
import android.graphics.Typeface
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import inc.flide.vim8.structures.FingerPosition
import android.widget.ImageButton
import inc.flide.vim8.structures.KeyboardAction
import inc.flide.vim8.structures.KeyboardActionType
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.Keyboard
import inc.flide.vim8.views.ButtonKeypadView
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener
import inc.flide.vim8.geometry.GeometricUtilities
import kotlin.jvm.JvmOverloads
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import org.xmlpull.v1.XmlPullParser
import kotlin.Throws
import org.xmlpull.v1.XmlPullParserException
import inc.flide.vim8.structures.KeyboardData
import inc.flide.vim8.keyboardHelpers.KeyboardDataXmlParser
import android.util.Xml
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper
import android.media.AudioManager
import inc.flide.vim8.keyboardActionListners.KeypadActionListener
import inc.flide.vim8.structures.MovementSequenceType
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.inputmethodservice.InputMethodService
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import android.os.IBinder
import android.text.TextUtils
import android.app.Application
import android.os.SystemClock
import android.text.InputType
import android.view.*
import android.view.inputmethod.*

class MainInputMethodService : InputMethodService() {
    private var inputConnection: InputConnection? = null
    private var editorInfo: EditorInfo? = null
    private var mainKeyboardView: MainKeyboardView? = null
    private var numberKeypadView: NumberKeypadView? = null
    private var selectionKeypadView: SelectionKeypadView? = null
    private var symbolKeypadView: SymbolKeypadView? = null
    private var currentKeypadView: View? = null
    private var shiftLockFlag = 0
    private var capsLockFlag = 0
    private var modifierFlags = 0
    private fun setCurrentKeypadView(view: View?) {
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
    override fun onCreateInputView(): View? {
        numberKeypadView = NumberKeypadView(this, null)
        selectionKeypadView = SelectionKeypadView(this, null)
        symbolKeypadView = SymbolKeypadView(this, null)
        mainKeyboardView = MainKeyboardView(this, null)
        setCurrentKeypadView(mainKeyboardView)
        return currentKeypadView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputConnection = currentInputConnection
    }

    override fun onBindInput() {
        inputConnection = currentInputConnection
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info
        val inputType = editorInfo.inputType and InputType.TYPE_MASK_CLASS
        when (inputType) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME -> switchToNumberPad()
            InputType.TYPE_CLASS_TEXT -> switchToMainKeypad()
            else -> switchToMainKeypad()
        }
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        inputConnection = currentInputConnection
        setShiftLockFlag(0)
        setCapsLockFlag(0)
        clearModifierFlags()
    }

    fun buildKeyboardActionMap(): KeyboardData? {
        return InputMethodServiceHelper.initializeKeyboardActionMap(resources, applicationContext)
    }

    fun sendText(text: String?) {
        inputConnection.commitText(text, 1)
        clearModifierFlags()
    }

    private fun clearModifierFlags() {
        modifierFlags = 0
    }

    fun sendDownKeyEvent(keyEventCode: Int, flags: Int) {
        inputConnection.sendKeyEvent(
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
        inputConnection.sendKeyEvent(
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

    fun switchToExternalEmoticonKeyboard() {
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val iBinder = this.window.window.getAttributes().token
        val keyboardId = getSelectedEmoticonKeyboardId()
        if (keyboardId.isEmpty()) {
            inputMethodManager.switchToLastInputMethod(iBinder)
        } else {
            inputMethodManager.setInputMethod(iBinder, keyboardId)
        }
    }

    private fun getSelectedEmoticonKeyboardId(): String? {
        val emoticonKeyboardId: String = SharedPreferenceHelper.Companion.getInstance(applicationContext)
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

    fun sendKey(keyEventCode: Int, flags: Int) {
        sendDownAndUpKeyEvent(keyEventCode, getShiftLockFlag() or getCapsLockFlag() or modifierFlags or flags)
        clearModifierFlags()
    }

    fun delete() {
        val sel = inputConnection.getSelectedText(0)
        if (TextUtils.isEmpty(sel)) {
            inputConnection.deleteSurroundingText(1, 0)
        } else {
            inputConnection.commitText("", 0)
        }
    }

    fun switchAnchor() {
        val extractedText = inputConnection.getExtractedText(ExtractedTextRequest(), InputConnection.GET_EXTRACTED_TEXT_MONITOR)
        val start = extractedText.selectionStart
        val end = extractedText.selectionEnd
        inputConnection.setSelection(end, start)
    }

    fun switchToSelectionKeypad() {
        setCurrentKeypadView(selectionKeypadView)
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
        inputConnection.performContextMenuAction(android.R.id.cut)
    }

    fun copy() {
        inputConnection.performContextMenuAction(android.R.id.copy)
    }

    fun paste() {
        inputConnection.performContextMenuAction(android.R.id.paste)
    }

    fun hideKeyboard() {
        requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun performShiftToggle() {
        //single press locks the shift key,
        //double press locks the caps key
        //a third press unlocks both.
        if (getShiftLockFlag() == KeyEvent.META_SHIFT_ON) {
            setShiftLockFlag(0)
            setCapsLockFlag(KeyEvent.META_CAPS_LOCK_ON)
        } else if (getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON) {
            setShiftLockFlag(0)
            setCapsLockFlag(0)
        } else {
            setShiftLockFlag(KeyEvent.META_SHIFT_ON)
            setCapsLockFlag(0)
        }
    }

    fun areCharactersCapitalized(): Boolean {
        return getShiftLockFlag() == KeyEvent.META_SHIFT_ON || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON
    }

    fun setModifierFlags(newModifierFlags: Int) {
        modifierFlags = modifierFlags or newModifierFlags
    }

    fun getShiftLockFlag(): Int {
        return shiftLockFlag
    }

    fun setShiftLockFlag(shiftLockFlag: Int) {
        this.shiftLockFlag = shiftLockFlag
        if (window.findViewById<View?>(R.id.xboardView) != null) {
            window.findViewById<View?>(R.id.xboardView).invalidate()
        }
    }

    fun getCapsLockFlag(): Int {
        return capsLockFlag
    }

    fun setCapsLockFlag(capsLockFlag: Int) {
        this.capsLockFlag = capsLockFlag
    }

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
    fun commitImeOptionsBasedEnter() {
        val imeAction = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        when (imeAction) {
            EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_PREVIOUS -> {
                val imeNoEnterFlag = editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION
                if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
                } else {
                    inputConnection.performEditorAction(imeAction)
                }
            }
            EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
            else -> sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)
        }
    }
}