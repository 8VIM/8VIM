package inc.flide.eightvim;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.List;
import java.util.Map;

import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.EightVimInputMethodServiceHelper;
import inc.flide.eightvim.keyboardHelpers.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.MainKeyboardView;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService {

    private MainKeyboardView mainKeyboardView;

    private NumberPadKeyboardView numberPadKeyboardView;


    private boolean isShiftLockOn;
    private boolean isCapsLockOn;

    EightVimInputMethodServiceHelper eightVimInputMethodServiceHelper = new EightVimInputMethodServiceHelper();

    @Override
    public View onCreateInputView() {
        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater()
                                    .inflate(R.layout.keyboard, null);
        mainKeyboardView = new MainKeyboardView(this);
        return mainKeyboardView;
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
        isShiftLockOn = false;
        isCapsLockOn = false;
    }

    public Map<List<FingerPosition>, KeyboardAction> buildKeyboardActionMap() {
        return eightVimInputMethodServiceHelper
                .initializeKeyboardActionMap(getResources(), getPackageName());
    }

    public void sendText(String str) {
        getCurrentInputConnection().commitText(str, 1);
    }

    public void sendKey(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1 && (isShiftLockOn || isCapsLockOn)){
            sendText(keyboardAction.getCapsLockText());
            isShiftLockOn = false;
        }else{
            sendText(keyboardAction.getText());
        }
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
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
            case SWITCH_TO_NUMBER_PAD:
                    setInputView(numberPadKeyboardView);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                    setInputView(mainKeyboardView);
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
