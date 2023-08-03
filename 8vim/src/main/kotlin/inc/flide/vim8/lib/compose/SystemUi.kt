package inc.flide.vim8.lib.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.SystemUiController
import inc.flide.vim8.lib.android.AndroidVersion
import inc.flide.vim8.theme.isLightTheme

@Composable
fun SystemUiApp() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.isLightTheme()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons,
        )
        if (AndroidVersion.ATLEAST_API26_O) {
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
                navigationBarContrastEnforced = false,
            )
        }
    }
}

@Composable
fun SystemUiIme() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.isLightTheme()
    val backgroundColor = MaterialTheme.colorScheme.background

    SideEffect {
        systemUiController.setStatusBarColor(
            color = backgroundColor,
            darkIcons = useDarkIcons,
        )
        if (AndroidVersion.ATLEAST_API26_O) {
            systemUiController.setNavigationBarColor(
                color = backgroundColor,
                darkIcons = useDarkIcons,
                navigationBarContrastEnforced = true,
            )
        }
    }
}

@Composable
private fun rememberSystemUiController(): SystemUiController {
    val view = LocalView.current
    return remember(view) { AppSystemUiController(view) }
}

private class AppSystemUiController(
    private val view: View,
) : SystemUiController {
    private val window = view.context.findWindow()!!
    private val windowInsetsController = WindowInsetsControllerCompat(window, view)

    override var systemBarsBehavior: Int
        get() = windowInsetsController.systemBarsBehavior
        set(value) {
            windowInsetsController.systemBarsBehavior = value
        }

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        statusBarDarkContentEnabled = darkIcons

        window.statusBarColor = when {
            darkIcons && !windowInsetsController.isAppearanceLightStatusBars -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }

            else -> color
        }.toArgb()
    }

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        navigationBarDarkContentEnabled = darkIcons
        isNavigationBarContrastEnforced = navigationBarContrastEnforced

        window.navigationBarColor = when {
            darkIcons && !windowInsetsController.isAppearanceLightNavigationBars -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }

            else -> color
        }.toArgb()
    }

    override var isStatusBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    override var isNavigationBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    override var statusBarDarkContentEnabled: Boolean
        get() = windowInsetsController.isAppearanceLightStatusBars
        set(value) {
            windowInsetsController.isAppearanceLightStatusBars = value
        }

    override var navigationBarDarkContentEnabled: Boolean
        get() = windowInsetsController.isAppearanceLightNavigationBars
        set(value) {
            windowInsetsController.isAppearanceLightNavigationBars = value
        }

    override var isNavigationBarContrastEnforced: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && window.isNavigationBarContrastEnforced
        set(value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = value
            }
        }

    private tailrec fun Context.findWindow(): Window? {
        val context = this
        if (context is Activity) return context.window
        if (context is InputMethodService) return context.window?.window
        return if (context is ContextWrapper) context.findWindow() else null
    }
}
