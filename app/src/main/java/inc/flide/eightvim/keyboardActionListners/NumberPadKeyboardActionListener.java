package inc.flide.eightvim.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.structures.KeyboardActionType;
import inc.flide.eightvim.views.NumberPadKeyboardView;

public class NumberPadKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;
    private NumberPadKeyboardView numberPadKeyboardView;

    public NumberPadKeyboardActionListener(EightVimInputMethodService inputMethodService
                                            , NumberPadKeyboardView view){
        this.eightVimInputMethodService = inputMethodService;
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
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
                eightVimInputMethodService.sendKey(primaryCode, 0);
                break;
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToSymbolsKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_SYMBOLS_KEYBOARD.toString()
                        , null, 0, 0);
                eightVimInputMethodService.handleSpecialInput(switchToSymbolsKeyboard);
                break;
            default:
                eightVimInputMethodService.sendText(String.valueOf((char)primaryCode));
        }

        numberPadKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void onText(CharSequence text) {
        eightVimInputMethodService.sendText(text.toString());
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
