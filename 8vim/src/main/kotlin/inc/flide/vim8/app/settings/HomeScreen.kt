package inc.flide.vim8.app.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.ListPreference
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.lib.compose.ErrorCard
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.WaringCard
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.app.Routes
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.utils.InputMethodUtils

@Composable
fun HomeScreen() = Screen {
    title = stringRes(R.string.settings__home__title)

    navigationIconVisible = false
    previewFieldVisible = true

    val navController = LocalNavController.current
    val context = LocalContext.current
    content {
        val is8VimEnabled by InputMethodUtils.observeIs8VimEnabled(foregroundOnly = true)
        val is8VimSelected by InputMethodUtils.observeIs8VimSelected(foregroundOnly = true)
        if (!is8VimEnabled) {
            ErrorCard(
                modifier = Modifier.padding(8.dp),
                showIcon = true,
                text = stringRes(R.string.settings__home__ime_not_enabled),
                onClick = { InputMethodUtils.showImeEnablerActivity(context) },
            )
        } else if (!is8VimSelected) {
            WaringCard(
                modifier = Modifier.padding(8.dp),
                showIcon = true,
                text = stringRes(R.string.settings__home__ime_not_selected),
                onClick = { InputMethodUtils.showImePicker(context) },
            )
        }
        Preference(
            iconId = R.drawable.ic_language,
            title = stringRes(R.string.settings__layouts__title),
            onClick = { navController.navigate(Routes.Settings.Layouts) },
        )
        PreferenceGroup(
            title = stringRes(R.string.pref_category_look_and_feel),
        ) {
            ListPreference(
                listPref = prefs.theme.mode,
                title = stringRes(id = R.string.pref_color_mode_title),
                entries = ThemeMode.listEntries()
            )
        }
    }
}