package inc.flide.vim8.ime.actionlisteners

import android.view.View
import com.hijamoya.keyboardview.KeyboardView
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.ime.layout.models.CustomKeycode

private const val NOT_A_KEY = -1

class ButtonKeypadActionListener(vim8ImeService: Vim8ImeService, view: View) :
    KeypadActionListener(vim8ImeService, view), KeyboardView.OnKeyboardActionListener {
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        when (primaryCode) {
            CustomKeycode.SHIFT_TOGGLE.keyCode -> handleShiftToggle()
            NOT_A_KEY -> {}
            else -> super.handleInputKey(primaryCode, 0)
        }
    }

    private fun handleShiftToggle() {
        if (vim8ImeService.shiftState == Vim8ImeService.State.OFF) {
            vim8ImeService.performShiftToggle()
        } else {
            vim8ImeService.shiftState = Vim8ImeService.State.OFF
        }
    }

    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {
        vim8ImeService.hideKeyboard()
    }

    override fun swipeUp() {}
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
}
