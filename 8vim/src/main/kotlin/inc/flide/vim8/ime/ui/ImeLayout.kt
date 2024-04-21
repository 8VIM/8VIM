package inc.flide.vim8.ime.ui

import androidx.compose.runtime.Composable
import inc.flide.vim8.ime.theme.ImeTheme

@Composable
fun ImeLayout(isFloating: Boolean, content: @Composable () -> Unit) {
    ImeTheme {
        if (isFloating) {
            FloatingImeLayout {
                content()
            }
        } else {
            content()
        }
    }
}
