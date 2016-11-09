package inc.flide.eightvim;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.List;
import java.util.Map;

import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.EightVimInputMethodServiceHelper;
import inc.flide.eightvim.keyboardHelpers.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.EightVimKeyboardView;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService {

    private EightVimKeyboardView eightVimKeyboardView;
    private boolean isEightVimKeyboardViewVisible;

    private NumberPadKeyboardView numberPadKeyboardView;


    private boolean isShiftLockOn;
    private boolean isCapsLockOn;
    Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

    EightVimInputMethodServiceHelper eightVimInputMethodServiceHelper = new EightVimInputMethodServiceHelper();

    @Override
    public View onCreateInputView() {
        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater()
                                    .inflate(R.layout.keyboard, null);
        eightVimKeyboardView = new EightVimKeyboardView(this);
        isEightVimKeyboardViewVisible = true;
        return eightVimKeyboardView;
    }

    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        super.onStartInput(attribute, restarting);
    }

    @Override
    public void onStartInputView (EditorInfo info, boolean restarting){
        super.onStartInputView(info, restarting);
    }

    @Override
    public void onInitializeInterface(){
        super.onInitializeInterface();
        keyboardActionMap = eightVimInputMethodServiceHelper
                            .initializeKeyboardActionMap(getResources(), getPackageName());
        isShiftLockOn = false;
        isCapsLockOn = false;
    }

    public void sendText(String str) {
        getCurrentInputConnection().commitText(str, 1);
    }

    public void sendKey(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    public void processMovementSequence(List<FingerPosition> movementSequence) {

        KeyboardAction keyboardAction = keyboardActionMap.get(movementSequence);

        boolean isMovementValid = true;
        if(keyboardAction == null){
            Logger.Verbose(this, "No Action Mapping has been defined for the given Sequence : " + movementSequence.toString());
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()){
            case INPUT_TEXT:
                handleInputText(keyboardAction);
                break;
            case INPUT_KEY:
                handleInputKey(keyboardAction);
                break;
            case INPUT_SPECIAL:
                handleSpecialInput(keyboardAction);
                break;
            default:
                Logger.Warn(this, "Action Type Undefined : " + keyboardAction.getKeyboardActionType().toString());
                isMovementValid = false;
                break;
        }
        if(isMovementValid){
            eightVimKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1 && (isShiftLockOn || isCapsLockOn)){
            sendText(keyboardAction.getCapsLockText());
            isShiftLockOn = false;
        }else{
            sendText(keyboardAction.getText());
        }
    }

    private void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode());
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode
                                                    .getInputSpecialKeyEventCodeWithName(
                                                            keyboardAction.getText());
        switch (keyeventCode){
            case SHIFT_TOOGLE:
                if(isShiftLockOn){
                    isShiftLockOn = false;
                    isCapsLockOn = true;
                } else if(isCapsLockOn){
                    isShiftLockOn = false;
                    isCapsLockOn = false;
                } else{
                    isShiftLockOn = true;
                    isCapsLockOn = false;
                }
                break;
            case KEYBOARD_TOOGLE:
                if(isEightVimKeyboardViewVisible){
                    isEightVimKeyboardViewVisible = false;
                    setInputView(numberPadKeyboardView);
                } else {
                    isEightVimKeyboardViewVisible = true;
                    setInputView(eightVimKeyboardView);
                }
                break;

            case PASTE:

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData primaryClipData = clipboardManager.getPrimaryClip();

                if(primaryClipData!=null && primaryClipData.getItemAt(0)!=null) {
                    sendText(primaryClipData.getItemAt(0).coerceToText(getApplicationContext()).toString());
                }
                break;

            default:
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getText());
                break;
        }
    }
}
