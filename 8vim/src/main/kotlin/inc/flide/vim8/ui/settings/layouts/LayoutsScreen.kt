package inc.flide.vim8.ui.settings.layouts

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.ListPreference
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.model.AvailableLayouts
import inc.flide.vim8.ui.LocalNavController
import inc.flide.vim8.ui.Routes

@Composable
fun LayoutsScreen() = Screen {
    title = stringRes(R.string.settings__layouts__title)

    previewFieldVisible = true
    iconSpaceReserved = false

    val navController = LocalNavController.current
    val context = LocalContext.current
    floatingActionButton {
        ExtendedFloatingActionButton(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Import"
                )
            },
            text = { Text(text = "Import") },
            onClick = { navController.navigate(Routes.Settings.LayoutImport) })
    }
    content {
        ListPreference(
            iconId = R.drawable.ic_language,
            listPref = prefs.layout.current,
            title = stringRes(id = R.string.settings__layouts__title),
            entries = AvailableLayouts.listEntries()
        )
    }
}