package inc.flide.eightvim.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.keyboardHelpers.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.eightvim.views.SelectionKeyboardView;

public class SelectionKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;
    private SelectionKeyboardView selectionKeyboardView;

    public SelectionKeyboardActionListener(EightVimInputMethodService inputMethodService
                                            , SelectionKeyboardView view){
        this.eightVimInputMethodService = inputMethodService;
        this.selectionKeyboardView = view;
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
                        KeyboardAction.KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.getName(),null
                        ,InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.getValue());
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
                eightVimInputMethodService.sendKey(primaryCode);
                break;
            default:
                eightVimInputMethodService.sendText(String.valueOf((char)primaryCode));
        }

        selectionKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    @Override
    public void onText(CharSequence text) {
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
