package inc.flide.vi8.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.structures.KeyboardActionType;
import inc.flide.vi8.views.SymbolKeyboardView;

public class SymbolKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private MainInputMethodService mainInputMethodService;
    private SymbolKeyboardView symbolKeyboardView;

    public SymbolKeyboardActionListener(MainInputMethodService inputMethodService
                                            , SymbolKeyboardView view){
        this.mainInputMethodService = inputMethodService;
        this.symbolKeyboardView = view;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        switch(primaryCode){
            case KeyEvent.KEYCODE_EISU:
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_TAB:
                mainInputMethodService.sendKey(primaryCode, 0);
                break;
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToNumberPadKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_NUMBER_PAD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToNumberPadKeyboardView);
                break;
            default:
                mainInputMethodService.sendText(String.valueOf((char)primaryCode));
        }

        symbolKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        symbolKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
