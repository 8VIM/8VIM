package inc.flide.vim8.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import inc.flide.vim8.app.settings.BackupRestoreScreen
import inc.flide.vim8.app.settings.ClipboardScreen
import inc.flide.vim8.app.settings.GestureScreen
import inc.flide.vim8.app.settings.HomeScreen
import inc.flide.vim8.app.settings.KeyboardScreen
import inc.flide.vim8.app.settings.LayoutScreen
import inc.flide.vim8.app.settings.ThemeScreen
import inc.flide.vim8.app.settings.about.AboutScreen
import inc.flide.vim8.app.settings.about.ThirdPartyLicencesScreen
import inc.flide.vim8.app.setup.SetupScreen

object Routes {
    object Setup {
        const val Screen = "setup"
    }

    object Settings {
        const val Home = "settings"
        const val Layouts = "settings/layouts"
        const val Keyboard = "settings/keyboard"
        const val Clipboard = "settings/clipboard"
        const val Theme = "settings/theme"
        const val Gesture = "settings/gesture"
        const val BackupAndRestore = "settings/backup-and-restore"
        const val About = "settings/about"
        const val ThirdPartyLicenses = "settings/about/third-party-licenses"
    }

    @Composable
    fun AppNavHost(
        modifier: Modifier,
        navController: NavHostController,
        startDestination: String
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Setup.Screen) { SetupScreen() }

            composable(Settings.Home) { HomeScreen() }
            composable(Settings.Layouts) { LayoutScreen() }
            composable(Settings.Keyboard) { KeyboardScreen() }
            composable(Settings.Clipboard) { ClipboardScreen() }
            composable(Settings.Theme) { ThemeScreen() }
            composable(Settings.Gesture) { GestureScreen() }
            composable(Settings.BackupAndRestore) { BackupRestoreScreen() }
            composable(Settings.About) { AboutScreen() }
            composable(Settings.ThirdPartyLicenses) { ThirdPartyLicencesScreen() }
        }
    }
}
