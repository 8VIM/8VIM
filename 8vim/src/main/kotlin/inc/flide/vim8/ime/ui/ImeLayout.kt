package inc.flide.vim8.ime.ui

import androidx.compose.runtime.Composable
import inc.flide.vim8.ime.theme.ImeTheme
import inc.flide.vim8.ime.ui.floating.compose.Layout

@Composable
fun ImeLayout(isFloating: Boolean, content: @Composable () -> Unit) {
    ImeTheme {
        if (isFloating) {
            Layout {
                content()
            }
        } else {
            content()
        }
    }
}
