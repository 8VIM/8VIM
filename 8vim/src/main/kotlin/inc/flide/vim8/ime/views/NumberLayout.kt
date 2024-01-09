package inc.flide.vim8.ime.views

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
import java.util.Currency
import java.util.Locale

@Composable
fun NumberLayout() {
    val currency = Currency.getInstance(Locale.getDefault());
    val keyboard = Keyboard(
        arrayOf(
            arrayOf(
                Key("#".toKeyboardAction()),
                Key(currency.symbol.toKeyboardAction()),
                Key("&".toKeyboardAction()),
                Key("(".toKeyboardAction()),
                Key(")".toKeyboardAction()),
                Key("1".toKeyboardAction()),
                Key("2".toKeyboardAction()),
                Key("3".toKeyboardAction()),
                Key("?".toKeyboardAction()),
            ),
            arrayOf(
                Key("@".toKeyboardAction()),
                Key("=".toKeyboardAction()),
                Key("_".toKeyboardAction()),
                Key("/".toKeyboardAction()),
                Key(
                    CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD.toKeyboardAction().copy(text = "|{^$|"),
                ),
                Key("4".toKeyboardAction()),
                Key("5".toKeyboardAction()),
                Key("6".toKeyboardAction()),
                Key("!".toKeyboardAction()),
            ),
            arrayOf(
                Key("%".toKeyboardAction()),
                Key("*".toKeyboardAction()),
                Key("-".toKeyboardAction()),
                Key("+".toKeyboardAction()),
                Key(
                    CustomKeycode.SWITCH_TO_MAIN_KEYPAD.toKeyboardAction(),
                    drawableId = R.drawable.ic_viii
                ),
                Key("7".toKeyboardAction()),
                Key("8".toKeyboardAction()),
                Key("9".toKeyboardAction()),
                Key(KeyEvent.KEYCODE_DEL.toKeyboardAction(), drawableId = R.drawable.ic_backspace),
            ),
            arrayOf(
                Key(":".toKeyboardAction()),
                Key("\"".toKeyboardAction()),
                Key("'".toKeyboardAction()),
                Key(" ".toKeyboardAction()),
                Key(",".toKeyboardAction()),
                Key("0".toKeyboardAction()),
                Key(".".toKeyboardAction()),
                Key(
                    KeyEvent.KEYCODE_ENTER.toKeyboardAction(),
                    drawableId = R.drawable.ic_keyboard_return
                ),
            ),
        )
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        KeyboardLayout(keyboard)
    }
}