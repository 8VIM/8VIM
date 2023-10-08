package inc.flide.vim8.ime.actionlisteners

import android.view.View
import com.hijamoya.keyboardview.KeyboardView
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.ime.layout.models.CustomKeycode

private const val NOT_A_KEY = -1

class ButtonKeypadActionListener(mainInputMethodService: MainInputMethodService, view: View) :
    KeypadActionListener(mainInputMethodService, view), KeyboardView.OnKeyboardActionListener {
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        when (primaryCode) {
            CustomKeycode.SHIFT_TOGGLE.keyCode -> handleShiftToggle()
            NOT_A_KEY -> {}
            else -> super.handleInputKey(primaryCode, 0)
        }
    }

    private fun handleShiftToggle() {
        if (mainInputMethodService.shiftState == MainInputMethodService.State.OFF) {
            mainInputMethodService.performShiftToggle()
        } else {
            mainInputMethodService.shiftState = MainInputMethodService.State.OFF
        }
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
