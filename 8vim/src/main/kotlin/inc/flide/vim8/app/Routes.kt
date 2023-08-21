package inc.flide.vim8.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import inc.flide.vim8.app.setup.SetupScreen
import inc.flide.vim8.lib.android.launchActivity
import inc.flide.vim8.ui.activities.SettingsActivity

object Routes {
    object Setup {
        const val Screen = "setup"
    }

    object Settings {
        const val Home = "settings"
    }

    @Composable
    fun AppNavHost(
        modifier: Modifier,
        navController: NavHostController,
        startDestination: String
    ) {
        val context = LocalContext.current
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Setup.Screen) { SetupScreen() }

            composable(Settings.Home) { context.launchActivity(SettingsActivity::class) }
        }
    }
}
