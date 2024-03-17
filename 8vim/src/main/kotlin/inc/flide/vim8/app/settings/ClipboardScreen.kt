package inc.flide.vim8.app.settings

import androidx.compose.runtime.Composable
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.datastore.ui.SliderPreference
import inc.flide.vim8.datastore.ui.SwitchPreference
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun ClipboardScreen() = Screen {
    title = stringRes(R.string.settings__clipboard__title)
    previewFieldVisible = true

    content {
        PreferenceGroup {
            SwitchPreference(
                pref = prefs.clipboard.enabled,
                title = stringRes(R.string.settings__keyboard__clipboard__enabled__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__clipboard__enabled__summary__off
                ),
                summaryOn = stringRes(R.string.settings__keyboard__clipboard__enabled__summary__on)
            )
            SliderPreference(
                pref = prefs.clipboard.maxHistory,
                title = stringRes(R.string.settings__keyboard__clipboard__max_history__title),
                min = 10,
                max = 100
            )
        }
    }
}
