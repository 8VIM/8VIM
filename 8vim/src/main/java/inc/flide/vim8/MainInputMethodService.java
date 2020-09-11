package inc.flide.vim8;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper;
import inc.flide.vim8.structures.InputSpecialKeyEventCode;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView;
import inc.flide.vim8.views.NumberPadKeyboardView;
import inc.flide.vim8.views.SelectionKeyboardView;
import inc.flide.vim8.views.SymbolKeyboardView;

public class MainInputMethodService extends InputMethodService {

    private MainKeyboardView mainKeyboardView;
    private NumberPadKeyboardView numberPadKeyboardView;
    private SelectionKeyboardView selectionKeyboardView;
    private SymbolKeyboardView symbolKeyboardView;
    private View currentView;

    private int shiftLockFlag;
    private int capsLockFlag;
    private int modifierFlags;

    InputMethodServiceHelper inputMethodServiceHelper = new InputMethodServiceHelper();
    InputConnection inputConnection;

    @Override
    public View onCreateInputView() {
        
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            IBinder iBinder = this.getWindow().getWindow().getAttributes().token;
            inputMethodManager.switchToLastInputMethod(iBinder);
        }

        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater()
                                    .inflate(R.layout.numberpad_keyboard_layout, null);
        selectionKeyboardView = (SelectionKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.selection_keyboard_layout, null);
        symbolKeyboardView = (SymbolKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.symbols_keyboard_layout, null);
        mainKeyboardView = (MainKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.main_keyboard_layout, null);
        currentView = mainKeyboardView.getView();

        return currentView;
    }

    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        super.onStartInput(attribute, restarting);
        inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onBindInput() {
        inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onStartInputView (EditorInfo info, boolean restarting){
        super.onStartInputView(info, restarting);
    }

    @Override
    public void onInitializeInterface(){
        super.onInitializeInterface();
        inputConnection = getCurrentInputConnection();
        setShiftLockFlag(0);
        setCapsLockFlag(0);
        clearModifierFlags();
    }

    public Map<List<FingerPosition>, KeyboardAction> buildKeyboardActionMap() {
        return inputMethodServiceHelper
                .initializeKeyboardActionMap(getResources(), getPackageName());
    }

    public void sendText(String text) {
        inputConnection.commitText(text, 1);
        clearModifierFlags();
    }

    public Context getContext() {
        return this;
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
    public void sendDownAndUpKeyEvent(int keyEventCode, int flags){
        sendDownKeyEvent(keyEventCode, flags);
        sendUpKeyEvent(keyEventCode, flags);
    }

    private void switchToExternalEmoticonKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        IBinder iBinder = this.getWindow().getWindow().getAttributes().token;
        String keyboardId = getSelectedEmoticonKeyboardId();
        if (keyboardId.isEmpty()){
            inputMethodManager.switchToLastInputMethod(iBinder);
        } else {
            inputMethodManager.setInputMethod(iBinder, getSelectedEmoticonKeyboardId());
        }

    }

    private String getSelectedEmoticonKeyboardId(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                    getString(R.string.basic_preference_file_name), Context.MODE_PRIVATE);
        String emoticonKeyboardId = sharedPreferences.getString(getString(R.string.bp_selected_emoticon_keyboard), "");
        //TODO: before returning verify that this keyboard Id we have does exist in the system.
        return emoticonKeyboardId;
    }

    public void sendKey(int keyEventCode , int flags) {
        sendDownAndUpKeyEvent(keyEventCode, (getShiftLockFlag() | getCapsLockFlag() | modifierFlags | flags));
        clearModifierFlags();
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1
                && (getShiftLockFlag() == KeyEvent.META_SHIFT_ON
                    || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON)){
            sendText(keyboardAction.getCapsLockText());
            setShiftLockFlag(0);
        }else{
            sendText(keyboardAction.getText());
        }
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
        setShiftLockFlag(0);
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode.valueOf(keyboardAction.getText());

        switch (keyeventCode){
            case SHIFT_TOOGLE:
                performShiftToogle();
                break;
            case SWITCH_TO_EMOJI_KEYBOARD:
                    switchToExternalEmoticonKeyboard();
                break;
            case SWITCH_TO_NUMBER_PAD:
                    currentView = numberPadKeyboardView;
                    setInputView(currentView);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                    currentView = mainKeyboardView.getView();
                    setInputView(currentView);
                break;
            case SWITCH_TO_SYMBOLS_KEYBOARD:
                    currentView = symbolKeyboardView;
                    setInputView(currentView);
                break;
            case PASTE:
                paste();
                break;
            case SELECTION_START:
                sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0);
                sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                break;
            case SWITCH_TO_SELECTION_KEYBOARD: {
                    currentView = selectionKeyboardView;
                    setInputView(currentView);
                }
                break;
            default:
                //Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getText());
                break;
        }
    }

    public void cut() { inputConnection.performContextMenuAction(android.R.id.cut); }

    public void copy() {
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    public void paste() { inputConnection.performContextMenuAction(android.R.id.paste); }

    private void performShiftToogle() {
        //single press locks the shift key,
        //double press locks the caps key
        //a third press unlocks both.
        if(getShiftLockFlag() == KeyEvent.META_SHIFT_ON){
            setShiftLockFlag(0);
            setCapsLockFlag(KeyEvent.META_CAPS_LOCK_ON);
        } else if(getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON){
            setShiftLockFlag(0);
            setCapsLockFlag(0);
        } else{
            setShiftLockFlag(KeyEvent.META_SHIFT_ON);
            setCapsLockFlag(0);
        }
    }

    public boolean areCharactersCapitalized(){
        if (getShiftLockFlag() == KeyEvent.META_SHIFT_ON || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON) {
            return true;
        }

        return false;
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
}
