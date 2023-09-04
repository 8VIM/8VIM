package inc.flide.vim8.ime.actionlisteners

import android.view.View
import com.hijamoya.keyboardview.KeyboardView
import inc.flide.vim8.MainInputMethodService

class ButtonKeypadActionListener(mainInputMethodService: MainInputMethodService, view: View) :
    KeypadActionListener(mainInputMethodService, view), KeyboardView.OnKeyboardActionListener {
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        super.handleInputKey(primaryCode, 0)
    }

    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {
        mainInputMethodService.hideKeyboard()
    }

    override fun swipeUp() {}
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
}
