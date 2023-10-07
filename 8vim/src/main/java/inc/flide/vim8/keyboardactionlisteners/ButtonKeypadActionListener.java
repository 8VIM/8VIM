package inc.flide.vim8.keyboardactionlisteners;

import android.view.View;
import com.hijamoya.keyboardview.KeyboardView;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.models.CustomKeycode;

public class ButtonKeypadActionListener extends KeypadActionListener implements KeyboardView.OnKeyboardActionListener {
    private static final int NOT_A_KEY = -1;

    public ButtonKeypadActionListener(MainInputMethodService mainInputMethodService, View view) {
        super(mainInputMethodService, view);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == NOT_A_KEY) {
            return;
        }
        if (primaryCode == CustomKeycode.SHIFT_TOGGLE.keyCode) {
            handleShiftToggle();
        } else if (primaryCode == CustomKeycode.CTRL_TOGGLE.keyCode) {
            mainInputMethodService.performCtrlToggle();
        } else {
            super.handleInputKey(primaryCode, 0);
        }
    }

    private void handleShiftToggle() {
        if (mainInputMethodService.getShiftState() == MainInputMethodService.State.OFF) {
            mainInputMethodService.performShiftToggle();
        } else {
            mainInputMethodService.setShiftState(MainInputMethodService.State.OFF);
        }
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
        mainInputMethodService.hideKeyboard();
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }
}
