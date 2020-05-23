package inc.flide.vi8.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.structures.KeyboardActionType;

public abstract class KeyboardActionListner implements KeyboardView.OnKeyboardActionListener {

    protected MainInputMethodService mainInputMethodService;
    protected View view;

    public KeyboardActionListner(MainInputMethodService mainInputMethodService, View view) {
        this.mainInputMethodService = mainInputMethodService;
        this.view = view;
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
                KeyboardAction switchToMainKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToMainKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
                mainInputMethodService.sendKey(primaryCode, 0);
                break;
            default:
                if (!ExtendedOnKey(primaryCode, keyCodes)) {
                    mainInputMethodService.sendText(String.valueOf((char) primaryCode));
                }
        }

        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    protected abstract boolean ExtendedOnKey(int primaryCode, int[] keyCodes);

    @Override
    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
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
