package inc.flide.vim8.ime.views

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.R
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.keyboard.text.Key
import inc.flide.vim8.ime.keyboard.text.Keyboard
import inc.flide.vim8.ime.keyboard.text.KeyboardLayout
import inc.flide.vim8.ime.keyboard.text.toKeyboardAction
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.keyboardManager

@Composable
fun SelectionLayout() {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val state by keyboardManager.activeState.collectAsState()

    val keyboard = Keyboard(
        arrayOf(
            arrayOf(
                Key(
                    KeyEvent.KEYCODE_CUT.toKeyboardAction(),
                    drawableId = R.drawable.ic_content_cut
                ),
                Key(
                    KeyEvent.KEYCODE_COPY.toKeyboardAction(),
                    drawableId = R.drawable.ic_content_copy
                ),
                Key(
                    KeyEvent.KEYCODE_FORWARD_DEL.toKeyboardAction(),
                    drawableId = R.drawable.ic_delete
                ),
                Key(
                    CustomKeycode.MOVE_CURRENT_END_POINT_UP.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_arrow_up
                ),
                Key(KeyEvent.KEYCODE_DEL.toKeyboardAction(), drawableId = R.drawable.ic_backspace)
            ),
            arrayOf(
                Key(
                    CustomKeycode.SHIFT_TOGGLE.toKeyboardAction(),
                    drawableId = when (state.inputShiftState) {
                        InputShiftState.UNSHIFTED -> R.drawable.ic_no_capslock
                        InputShiftState.SHIFTED -> R.drawable.ic_shift_engaged
                        InputShiftState.CAPS_LOCK -> R.drawable.ic_capslock_engaged
                    }
                ),
                Key(
                    CustomKeycode.SWITCH_TO_MAIN_KEYPAD.toKeyboardAction(),
                    drawableId = R.drawable.ic_viii
                ),
                Key(
                    CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_arrow_left
                ),
                Key(
                    CustomKeycode.TOGGLE_SELECTION_ANCHOR.toKeyboardAction(),
                    drawableId = R.drawable.pad_center
                ),
                Key(
                    CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_arrow_right
                )
            ),
            arrayOf(
                Key(
                    CustomKeycode.SELECT_ALL.toKeyboardAction(),
                    drawableId = R.drawable.ic_select_all
                ),
                Key(
                    KeyEvent.KEYCODE_PASTE.toKeyboardAction(),
                    drawableId = R.drawable.ic_content_paste
                ),
                Key(
                    CustomKeycode.CTRL_TOGGLE.toKeyboardAction(),
                    drawableId = if (state.isCtrlOn) {
                        R.drawable.ic_ctrl_engaged
                    } else {
                        R.drawable.ic_ctrl
                    }
                ),
                Key(
                    CustomKeycode.MOVE_CURRENT_END_POINT_DOWN.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_arrow_down
                ),
                Key(
                    KeyEvent.KEYCODE_ENTER.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_return
                )
            )
        )
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        KeyboardLayout(keyboard)
    }
}
