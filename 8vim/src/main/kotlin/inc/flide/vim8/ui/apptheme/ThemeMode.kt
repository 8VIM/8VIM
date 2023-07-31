package inc.flide.vim8.ui.apptheme

import androidx.compose.runtime.Composable
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.listPrefEntries
import inc.flide.vim8.lib.compose.stringRes

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    CUSTOM("custom");

    companion object {
        @Composable
        fun listEntries() = listPrefEntries {
            entry(
                key = SYSTEM,
                label = stringRes(R.string.enum__theme_mode__follow_system)
            )
            entry(
                key = LIGHT,
                label = stringRes(R.string.enum__theme_mode__always_light)
            )
            entry(
                key = DARK,
                label = stringRes(R.string.enum__theme_mode__always_dark)
            )
            entry(
                key = CUSTOM,
                label = stringRes(R.string.enum__theme_mode__custom)
            )
        }
    }
}