package inc.flide.eightvim;

import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.List;
import java.util.Map;

import inc.flide.eightvim.structures.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.EightVimInputMethodServiceHelper;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.MainKeyboardView;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.eightvim.views.SelectionKeyboardView;
import inc.flide.eightvim.views.SymbolKeyboardView;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService {

    private MainKeyboardView mainKeyboardView;
    private NumberPadKeyboardView numberPadKeyboardView;
    private SelectionKeyboardView selectionKeyboardView;
    private SymbolKeyboardView symbolKeyboardView;
    private View currentView;



    private int isShiftLockOn;
    private int isCapsLockOn;
    private boolean shouldSkipImmediateNextCharacter;

    EightVimInputMethodServiceHelper eightVimInputMethodServiceHelper = new EightVimInputMethodServiceHelper();
    InputConnection inputConnection;

    @Override
    public View onCreateInputView() {
        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater()
                                    .inflate(R.layout.numberpad_keyboard_layout, null);
        selectionKeyboardView = (SelectionKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.selection_keyboard_layout, null);
        symbolKeyboardView = (SymbolKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.symbols_keyboard_layout, null);
        mainKeyboardView = new MainKeyboardView(this);
        currentView = mainKeyboardView;
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
        isShiftLockOn = 0;
        isCapsLockOn = 0;
        shouldSkipImmediateNextCharacter = false;
        inputConnection = getCurrentInputConnection();
    }

    public Map<List<FingerPosition>, KeyboardAction> buildKeyboardActionMap() {
        return eightVimInputMethodServiceHelper
                .initializeKeyboardActionMap(getResources(), getPackageName());
    }

    public void sendText(String text) {
        if(shouldSkipImmediateNextCharacter){
            shouldSkipImmediateNextCharacter = false;
            return;
        }
        inputConnection.commitText(text, 1);
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

    public void sendKey(int keyEventCode , int flags) {

        sendDownAndUpKeyEvent(keyEventCode, (isShiftLockOn | isCapsLockOn | flags));

        if (shouldSkipImmediateNextCharacter) {
            shouldSkipImmediateNextCharacter = false;
        }
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1
                && (isShiftLockOn == KeyEvent.META_SHIFT_ON
                    || isCapsLockOn == KeyEvent.META_CAPS_LOCK_ON)){
            sendText(keyboardAction.getCapsLockText());
            isShiftLockOn = 0;
        }else{
            sendText(keyboardAction.getText());
        }
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
        isShiftLockOn = 0;
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode.valueOf(keyboardAction.getText());

        switch (keyeventCode){
            case SHIFT_TOOGLE:
                performShiftToogle();
                break;
            case SWITCH_TO_NUMBER_PAD:
                    currentView = numberPadKeyboardView;
                    setInputView(currentView);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                    currentView = mainKeyboardView;
                    setInputView(currentView);
                break;
            case SWITCH_TO_SYMBOLS_KEYBOARD:
                    Logger.d(this, "switching to symbols keyboard");
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
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getText());
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
        if(isShiftLockOn == KeyEvent.META_SHIFT_ON){
            isShiftLockOn = 0;
            isCapsLockOn = KeyEvent.META_CAPS_LOCK_ON;
        } else if(isCapsLockOn == KeyEvent.META_CAPS_LOCK_ON){
            isShiftLockOn = 0;
            isCapsLockOn = 0;
        } else{
            isShiftLockOn = KeyEvent.META_SHIFT_ON;
            isCapsLockOn = 0;
        }
    }
}
