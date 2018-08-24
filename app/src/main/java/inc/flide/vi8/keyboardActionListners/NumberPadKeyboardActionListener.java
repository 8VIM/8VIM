package inc.flide.vi8.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.KeyboardActionType;
import inc.flide.vi8.views.NumberPadKeyboardView;

public class NumberPadKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private MainInputMethodService mainInputMethodService;
    private NumberPadKeyboardView numberPadKeyboardView;

    public NumberPadKeyboardActionListener(MainInputMethodService inputMethodService
                                            , NumberPadKeyboardView view){
        this.mainInputMethodService = inputMethodService;
        this.numberPadKeyboardView = view;
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
                mainInputMethodService.sendKey(primaryCode, 0);
                break;
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToSymbolsKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_SYMBOLS_KEYBOARD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToSymbolsKeyboard);
                break;
            default:
                mainInputMethodService.sendText(String.valueOf((char)primaryCode));
        }

        numberPadKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        numberPadKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
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
