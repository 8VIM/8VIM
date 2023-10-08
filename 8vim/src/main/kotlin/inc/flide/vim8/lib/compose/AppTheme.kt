package inc.flide.vim8.lib.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import inc.flide.vim8.theme.typography

@Composable
fun AppTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = colorScheme.typography(),
        content = content
    )
}
