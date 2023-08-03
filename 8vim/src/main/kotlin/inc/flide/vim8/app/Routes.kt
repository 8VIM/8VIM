package inc.flide.vim8.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import inc.flide.vim8.app.settings.HomeScreen
import inc.flide.vim8.app.settings.layouts.LayoutsScreen
import inc.flide.vim8.app.setup.SetupScreen

object Routes {
    object Setup {
        const val Screen = "setup"
    }

    object Settings {
        const val Home = "settings"
        const val Layouts = "settings/layouts"
        const val LayoutImport = "settings/layouts/import"
    }

    @Composable
    fun AppNavHost(
        modifier: Modifier,
        navController: NavHostController,
        startDestination: String,
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Setup.Screen) { SetupScreen() }

            composable(Settings.Home) { HomeScreen() }

            composable(Settings.Layouts) { LayoutsScreen() }
        }
    }
}