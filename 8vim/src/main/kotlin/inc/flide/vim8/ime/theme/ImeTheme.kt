package inc.flide.vim8.ime.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.lib.compose.AppTheme
import inc.flide.vim8.lib.observeAsNonNullState
import inc.flide.vim8.themeManager

private val LocalTheme = staticCompositionLocalOf<ThemeManager.ThemeInfo> { error("not init") }

object ImeTheme {
    val current: ThemeManager.ThemeInfo
        @Composable
        @ReadOnlyComposable
        get() = LocalTheme.current
}

@Composable
fun ImeTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themeManager by context.themeManager()

    val currentTheme by themeManager.currentTheme.observeAsNonNullState()
    val current = remember(currentTheme) { currentTheme }

    AppTheme(colorScheme = current.scheme) {
        CompositionLocalProvider(LocalTheme provides current) {
            content()
        }
    }
}
