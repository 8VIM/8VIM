package inc.flide.vim8.app.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import inc.flide.vim8.BuildConfig
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.app.Routes
import inc.flide.vim8.app.Urls
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.lib.android.launchUrl
import inc.flide.vim8.lib.android.shareApp
import inc.flide.vim8.lib.compose.AppIcon
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun HomeScreen() = Screen {
    title = stringRes(R.string.settings__home__title)
    navigationIconVisible = false
    previewFieldVisible = true

    val navController = LocalNavController.current
    val context = LocalContext.current

    content {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            AppIcon()
        }
        Preference(
            iconId = R.drawable.ic_language,
            title = stringRes(R.string.settings__layouts__title),
            onClick = { navController.navigate(Routes.Settings.Layouts) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )

        Preference(
            iconId = R.drawable.ic_keyboard,
            title = stringRes(R.string.settings__keyboard__title),
            onClick = { navController.navigate(Routes.Settings.Keyboard) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
        Preference(
            iconId = R.drawable.ic_outline_palette,
            title = stringRes(R.string.settings__theme__title),
            onClick = { navController.navigate(Routes.Settings.Theme) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
        Preference(
            iconId = R.drawable.ic_gesture,
            title = stringRes(R.string.settings__gesture__title),
            onClick = { navController.navigate(Routes.Settings.Gesture) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
        Preference(
            iconId = R.drawable.ic_settings_backup_restore,
            title = stringRes(R.string.settings__backup_and_restore__title),
            onClick = { navController.navigate(Routes.Settings.BackupAndRestore) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
        Preference(
            iconId = R.drawable.ic_error_outline,
            title = stringRes(R.string.about__title),
            onClick = { navController.navigate(Routes.Settings.About) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
        Preference(
            iconId = R.drawable.ic_help,
            title = stringRes(R.string.settings__help__feedback),
            onClick = { context.launchUrl(Urls.MAIL_TO) },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )

        Preference(
            iconId = R.drawable.ic_share,
            title = stringRes(R.string.share_app),
            onClick = {
                context.shareApp(
                    """
Check out this awesome keyboard application

https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
"""
                )
            },
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        )
    }
}
