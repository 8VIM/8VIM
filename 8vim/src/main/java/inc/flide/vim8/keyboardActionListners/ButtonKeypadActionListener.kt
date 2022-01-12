package inc.flide.vim8.keyboardActionListners

import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.view.View
import inc.flide.vim8.MainInputMethodService

class ButtonKeypadActionListener(view: View) : KeypadActionListener(view), OnKeyboardActionListener {
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        super.handleInputKey(primaryCode, 0)
    }

    override fun onText(text: CharSequence?) {
        super.onText(text)
    }
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {
        MainInputMethodService.hideKeyboard()
    }

    override fun swipeUp() {}
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
}