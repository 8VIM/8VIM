package inc.flide.vim8.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.View;

import inc.flide.vim8.MainInputMethodService;

public class ButtonKeypadActionListener extends KeypadActionListener implements KeyboardView.OnKeyboardActionListener {

    public ButtonKeypadActionListener(MainInputMethodService mainInputMethodService, View view) {
        super(mainInputMethodService, view);
    }

    @Override
    public void onText(CharSequence text) {
       super.onText(text);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        super.onKey(primaryCode, keyCodes);
    }

    @Override
    public void swipeLeft() { }

    @Override
    public void swipeRight() { }

    @Override
    public void swipeDown() { }

    @Override
    public void swipeUp() { }

    @Override
    public void onPress(int primaryCode) { }

    @Override
    public void onRelease(int primaryCode) { }
}
