package inc.flide.eightvim;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Map;

import inc.flide.eightvim.structures.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.EightVimInputMethodServiceHelper;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.MainKeyboardView;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.eightvim.views.SelectionKeyboardView;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService {

    private MainKeyboardView mainKeyboardView;
    private NumberPadKeyboardView numberPadKeyboardView;
    private SelectionKeyboardView selectionKeyboardView;
    private View curentView;


    private boolean isShiftLockOn;
    private boolean isCapsLockOn;
    private boolean shouldSkipImmediateNextCharacter;

    EightVimInputMethodServiceHelper eightVimInputMethodServiceHelper = new EightVimInputMethodServiceHelper();

    @Override
    public View onCreateInputView() {
        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater()
                                    .inflate(R.layout.numberpad_keyboard_layout, null);
        selectionKeyboardView = (SelectionKeyboardView) getLayoutInflater()
                                    .inflate(R.layout.selection_keyboard_layout, null);
        mainKeyboardView = new MainKeyboardView(this);
        curentView = mainKeyboardView;
        return curentView;
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
        shouldSkipImmediateNextCharacter = false;
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
        getCurrentInputConnection().commitText(text, 1);
    }

    public void sendKey(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        shouldSkipImmediateNextCharacter = false;
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

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode.valueOf(keyboardAction.getText());

        switch (keyeventCode){
            case SHIFT_TOOGLE:
                performShiftToogle();
                break;
            case SWITCH_TO_NUMBER_PAD:
                    curentView = numberPadKeyboardView;
                    setInputView(curentView);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                    curentView = mainKeyboardView;
                    setInputView(curentView);
                break;
            case PASTE: {

                    ClipboardManager clipboardManager = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData primaryClipData = clipboardManager.getPrimaryClip();

                    if (primaryClipData != null && primaryClipData.getItemAt(0) != null) {
                        sendText(primaryClipData
                                .getItemAt(0)
                                .coerceToText(getApplicationContext())
                                .toString());
                        shouldSkipImmediateNextCharacter = true;
                    }
                }
                break;
            case SELECTION_START: {

                InputConnection inputConnection = getCurrentInputConnection();
                ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
                inputConnection.setSelection(extractedText.selectionStart-1,extractedText.selectionEnd);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.updateSelection(curentView, extractedText.selectionStart-1,extractedText.selectionEnd, 0, 0);

                }
                break;
            case SWITCH_TO_SELECTION_KEYBOARD: {
                    curentView = selectionKeyboardView;
                    setInputView(curentView);
                }
                break;
            default:
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getText());
                break;
        }
    }

    private void performShiftToogle() {
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
    }
}
