package inc.flide.vim8;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.autofill.CharSequenceTransformation;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Map;

import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.views.NumberKeypadView;
import inc.flide.vim8.views.SelectionKeypadView;
import inc.flide.vim8.views.SymbolKeypadView;
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView;

public class MainInputMethodService extends InputMethodService {

    private InputConnection inputConnection;
    private EditorInfo editorInfo;

    private MainKeyboardView mainKeyboardView;
    private NumberKeypadView numberKeypadView;
    private SelectionKeypadView selectionKeypadView;
    private SymbolKeypadView symbolKeypadView;
    private View currentKeypadView;

    private int shiftLockFlag;
    private int capsLockFlag;
    private int modifierFlags;

    private void setCurrentKeypadView(View view){
        this.currentKeypadView = view;
        currentKeypadView.invalidate();
        setInputView(currentKeypadView);
    }

    /**
     * Lifecycle of IME
     * <p>
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

    @Override
    public View onCreateInputView() {
        numberKeypadView = new NumberKeypadView(this, null);
        selectionKeypadView = new SelectionKeypadView(this, null);
        symbolKeypadView = new SymbolKeypadView(this, null);
        mainKeyboardView = new MainKeyboardView(this, null);
        setCurrentKeypadView(mainKeyboardView);
        return currentKeypadView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        this.inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onBindInput() {
        inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setCurrentKeypadView(mainKeyboardView);
        this.editorInfo = info;
    }

    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
        inputConnection = getCurrentInputConnection();
        setShiftLockFlag(0);
        setCapsLockFlag(0);
        clearModifierFlags();
    }

    public Map<List<FingerPosition>, KeyboardAction> buildKeyboardActionMap() {
        return InputMethodServiceHelper.initializeKeyboardActionMap(getResources(), getApplicationContext());
    }

    public void sendText(String text) {
        inputConnection.commitText(text, 1);
        clearModifierFlags();
    }

    private void clearModifierFlags() {
        modifierFlags = 0;
    }

    public void sendDownKeyEvent(int keyEventCode, int flags) {
        inputConnection.sendKeyEvent(
                new KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN,
                        keyEventCode,
                        0,
                        flags
                )
        );
    }

    public void sendUpKeyEvent(int keyEventCode, int flags) {
        inputConnection.sendKeyEvent(
                new KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        keyEventCode,
                        0,
                        flags
                )
        );
    }

    public void sendDownAndUpKeyEvent(int keyEventCode, int flags) {
        sendDownKeyEvent(keyEventCode, flags);
        sendUpKeyEvent(keyEventCode, flags);
    }

    public void switchToExternalEmoticonKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder iBinder = this.getWindow().getWindow().getAttributes().token;
        String keyboardId = getSelectedEmoticonKeyboardId();
        if (keyboardId.isEmpty()) {
            inputMethodManager.switchToLastInputMethod(iBinder);
        } else {
            inputMethodManager.setInputMethod(iBinder, keyboardId);
        }

    }

    private String getSelectedEmoticonKeyboardId() {
        String emoticonKeyboardId = SharedPreferenceHelper
                .getInstance(getApplicationContext())
                .getString(getString(R.string.pref_selected_emoticon_keyboard), "");

        // Before returning verify that this keyboard Id we have does exist in the system.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.getEnabledInputMethodList();
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().compareTo(emoticonKeyboardId) == 0) {
                return emoticonKeyboardId;
            }
        }
        return "";
    }

    public void sendKey(int keyEventCode, int flags) {
        sendDownAndUpKeyEvent(keyEventCode, getShiftLockFlag() | getCapsLockFlag() | modifierFlags | flags);
        clearModifierFlags();
    }

    public void delete() {
        CharSequence sel = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(sel)) {
            inputConnection.deleteSurroundingText(1, 0);
        } else {
            inputConnection.commitText("", 0);
        }
    }


    public void switchToSelectionKeypad() { setCurrentKeypadView(selectionKeypadView); }

    public void switchToSymbolsKeypad() { setCurrentKeypadView(symbolKeypadView); }

    public void switchToMainKeypad() { setCurrentKeypadView(mainKeyboardView); }

    public void switchToNumberPad() { setCurrentKeypadView(numberKeypadView); }

    public void cut() {
        inputConnection.performContextMenuAction(android.R.id.cut);
    }

    public void copy() {
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    public void paste() {
        inputConnection.performContextMenuAction(android.R.id.paste);
    }

    public void hideKeyboard() {
        this.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void performShiftToggle() {
        //single press locks the shift key,
        //double press locks the caps key
        //a third press unlocks both.
        if (getShiftLockFlag() == KeyEvent.META_SHIFT_ON) {
            setShiftLockFlag(0);
            setCapsLockFlag(KeyEvent.META_CAPS_LOCK_ON);
        } else if (getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON) {
            setShiftLockFlag(0);
            setCapsLockFlag(0);
        } else {
            setShiftLockFlag(KeyEvent.META_SHIFT_ON);
            setCapsLockFlag(0);
        }
    }

    public boolean areCharactersCapitalized() {
        return getShiftLockFlag() == KeyEvent.META_SHIFT_ON || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }

    public void setModifierFlags(int newModifierFlags) {
        this.modifierFlags = this.modifierFlags | newModifierFlags;
    }

    public int getShiftLockFlag() {
        return shiftLockFlag;
    }

    public void setShiftLockFlag(int shiftLockFlag) {
        this.shiftLockFlag = shiftLockFlag;
        if (getWindow().findViewById(R.id.xboardView) != null) {
            getWindow().findViewById(R.id.xboardView).invalidate();
        }
    }

    public int getCapsLockFlag() {
        return capsLockFlag;
    }

    public void setCapsLockFlag(int capsLockFlag) {
        this.capsLockFlag = capsLockFlag;
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
    public void commitImeOptionsBasedEnter() {
        int imeAction = this.editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        switch(imeAction) {
            case EditorInfo.IME_ACTION_GO:
            case EditorInfo.IME_ACTION_SEARCH:
            case EditorInfo.IME_ACTION_SEND:
            case EditorInfo.IME_ACTION_NEXT:
            case EditorInfo.IME_ACTION_DONE:
            case EditorInfo.IME_ACTION_PREVIOUS:
                int imeNoEnterFlag = this.editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0);
                } else {
                    inputConnection.performEditorAction(imeAction);
                }
                break;
            case EditorInfo.IME_ACTION_UNSPECIFIED:
            case EditorInfo.IME_ACTION_NONE:
            default:
                sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0);
        }
    }

}
