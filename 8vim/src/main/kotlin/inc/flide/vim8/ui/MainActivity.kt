package inc.flide.vim8.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.patrickgold.jetpref.datastore.model.observeAsState
import inc.flide.vim8.lib.compose.LocalPreviewFieldController
import inc.flide.vim8.lib.compose.PreviewKeyboardField
import inc.flide.vim8.lib.compose.ProvideLocalizedResources
import inc.flide.vim8.lib.compose.SystemUiApp
import inc.flide.vim8.lib.compose.rememberPreviewFieldController
import inc.flide.vim8.ui.apptheme.AppTheme

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("LocalNavController not initialized")
}

class MainActivity : ComponentActivity() {
    private val prefs by appPreferenceModel()
    private var resourcesContext by mutableStateOf(this as Context)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { !prefs.datastoreReadyStatus.get() }
        }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        resourcesContext = createConfigurationContext(Configuration(resources.configuration))

        prefs.datastoreReadyStatus.observe(this) { isModelLoaded ->
            if (!isModelLoaded) return@observe
            setContent {
                ProvideLocalizedResources(resourcesContext) {
                    AppTheme {
                        Surface {
                            SystemUiApp()
                            AppContent()
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun AppContent() {
        val navController = rememberNavController()
        val previewFieldController = rememberPreviewFieldController()
        val isImeSetUp by prefs.internal.isImeSetup.observeAsState()
        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalPreviewFieldController provides previewFieldController,
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                Routes.AppNavHost(
                    modifier = Modifier.weight(1.0f),
                    navController = navController,
                    startDestination = if (isImeSetUp) Routes.Settings.Home else Routes.Setup.Screen,
                )
                PreviewKeyboardField(previewFieldController)
            }
        }
        SideEffect {
            navController.setOnBackPressedDispatcher(this.onBackPressedDispatcher)
        }
    }
}