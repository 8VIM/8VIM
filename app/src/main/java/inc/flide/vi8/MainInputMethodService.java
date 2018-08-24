package inc.flide.vi8;

import android.content.Context;
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

import inc.flide.vi8.structures.FingerPosition;
import inc.flide.vi8.keyboardHelpers.InputMethodServiceHelper;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.views.mainKeyboard.MainKeyboardView;
import inc.flide.vi8.views.NumberPadKeyboardView;
import inc.flide.vi8.views.SelectionKeyboardView;
import inc.flide.vi8.views.SymbolKeyboardView;

public class MainInputMethodService extends InputMethodService {

    private MainKeyboardView mainKeyboardView;
    private NumberPadKeyboardView numberPadKeyboardView;
    private SelectionKeyboardView selectionKeyboardView;
    private SymbolKeyboardView symbolKeyboardView;
    private View currentView;

    private int isShiftLockOn;
    private int isCapsLockOn;
    private int modifierFlags;

    InputMethodServiceHelper inputMethodServiceHelper = new InputMethodServiceHelper();
    InputConnection inputConnection;

    @Override
    public View onCreateInputView() {

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
        isShiftLockOn = 0;
        isCapsLockOn = 0;
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

    public int getDrawableResourceId(String resourceString) {
        return getContext().getResources()
                .getIdentifier(resourceString, "drawable", getPackageName());

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

    private void switchToExternalEmojiKeyboard() {
        //Check if the user has ever set up the emoji keyboard?
            // if no then display a dialog box and allow him to select one of the available keyboards or previous keyboard
            // if yes then try switching to external keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        IBinder iBinder = this.getWindow().getWindow().getAttributes().token;

        inputMethodManager.setInputMethod(iBinder, getSelectedEmojiKeyboardId());

        //Following piece of code finds all available ime's
        /*
        final InputMethodManager imeManager = (InputMethodManager)eightVimInputMethodService.getApplicationContext().getSystemService(eightVimInputMethodService.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        Map<String, String> inputMethodsNameAndId = new HashMap<>();
        for (InputMethodInfo inputMethodInfo: inputMethods){
            inputMethodsNameAndId.put(inputMethodInfo.loadLabel(eightVimInputMethodService.getPackageManager()).toString(), inputMethodInfo.getId());
        }
        */
    }

    private String getSelectedEmojiKeyboardId(){
        //if the selected keyboard from the user preference settings is no longer available,
        //return the id of the last keyboard
        return "inc.flide.emojiKeyboard/inc.flide.ime.EmojiKeyboardService";
    }

    public void sendKey(int keyEventCode , int flags) {
        sendDownAndUpKeyEvent(keyEventCode, (isShiftLockOn | isCapsLockOn | modifierFlags | flags));
        clearModifierFlags();
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1
                && (isShiftLockOn == KeyEvent.META_SHIFT_ON
                    || isCapsLockOn == KeyEvent.META_CAPS_LOCK_ON)){
            sendText(keyboardAction.getCapsLockText());
            isShiftLockOn = 0;
            currentView.invalidate();
        }else{
            sendText(keyboardAction.getText());
        }
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
        isShiftLockOn = 0;
        currentView.invalidate();
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode.valueOf(keyboardAction.getText());

        switch (keyeventCode){
            case SHIFT_TOOGLE:
                performShiftToogle();
                currentView.invalidate();
                break;
            case SWITCH_TO_EMOJI_KEYBOARD:
                    switchToExternalEmojiKeyboard();
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

    public boolean areCharactersCapitalized(){
        if (isShiftLockOn == KeyEvent.META_SHIFT_ON || isCapsLockOn == KeyEvent.META_CAPS_LOCK_ON) {
            return true;
        }

        return false;
    }

    public void setModifierFlags(int newModifierFlags) {
        this.modifierFlags = this.modifierFlags | newModifierFlags;
    }
}
