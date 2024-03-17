package inc.flide.vim8.ime.keyboard.view

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import inc.flide.vim8.R
import inc.flide.vim8.ime.keyboard.text.Key
import inc.flide.vim8.ime.keyboard.text.Keyboard
import inc.flide.vim8.ime.keyboard.text.KeyboardLayout
import inc.flide.vim8.ime.keyboard.text.toKeyboardAction
import inc.flide.vim8.ime.layout.models.CustomKeycode

@Composable
fun SymbolsLayout() {
    val keyboard = Keyboard(
        arrayOf(
            arrayOf(
                Key("£".toKeyboardAction()),
                Key("€".toKeyboardAction()),
                Key("$".toKeyboardAction()),
                Key("¢".toKeyboardAction()),
                Key(
                    KeyEvent.KEYCODE_TAB.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_tab
                ),
                Key("©".toKeyboardAction()),
                Key("®".toKeyboardAction()),
                Key("™".toKeyboardAction()),
                Key("¿".toKeyboardAction())
            ),
            arrayOf(
                Key("[".toKeyboardAction()),
                Key("]".toKeyboardAction()),
                Key("{".toKeyboardAction()),
                Key("}".toKeyboardAction()),
                Key(
                    CustomKeycode.SWITCH_TO_NUMBER_KEYPAD.toKeyboardAction(),
                    drawableId = R.drawable.numericpad_vd_vector
                ),
                Key("<".toKeyboardAction()),
                Key(">".toKeyboardAction()),
                Key("^".toKeyboardAction()),
                Key("¡".toKeyboardAction())
            ),
            arrayOf(
                Key("§".toKeyboardAction()),
                Key("¬".toKeyboardAction()),
                Key("¶".toKeyboardAction()),
                Key("°".toKeyboardAction()),
                Key(
                    CustomKeycode.SWITCH_TO_MAIN_KEYPAD.toKeyboardAction(),
                    drawableId = R.drawable.ic_viii
                ),
                Key("\\".toKeyboardAction()),
                Key("|".toKeyboardAction()),
                Key("¦".toKeyboardAction()),
                Key(KeyEvent.KEYCODE_DEL.toKeyboardAction(), drawableId = R.drawable.ic_backspace)
            ),
            arrayOf(
                Key("~".toKeyboardAction()),
                Key("⨯".toKeyboardAction()),
                Key("÷".toKeyboardAction()),
                Key(" ".toKeyboardAction(), alternateText = "┗━┛"),
                Key("`".toKeyboardAction()),
                Key(";".toKeyboardAction()),
                Key(":".toKeyboardAction()),
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
