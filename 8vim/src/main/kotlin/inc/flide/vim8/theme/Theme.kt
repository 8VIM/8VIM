package inc.flide.vim8.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import inc.flide.vim8.lib.android.AndroidVersion

fun darkColorPalette(context: Context) =
    if (AndroidVersion.ATLEAST_API31_S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme()
    }

fun lightColorPalette(context: Context) =
    if (AndroidVersion.ATLEAST_API31_S) {
        dynamicLightColorScheme(context)
    } else {
        lightColorScheme()
    }

@Composable
fun AppTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = colorScheme.typography(),
        content = content,
    )
}

fun ColorScheme.isLightTheme() = this.background.luminance() >= 0.5f
fun ColorScheme.typography(): Typography {
    val current = Typography()
    return current.copy(
        displayLarge = current.displayLarge.copy(color = onBackground),
        displayMedium = current.displayMedium.copy(color = onBackground),
        displaySmall = current.displaySmall.copy(color = onBackground),
        titleLarge = current.titleLarge.copy(color = onBackground),
        titleMedium = current.titleMedium.copy(color = onBackground),
        titleSmall = current.titleSmall.copy(color = onBackground),
        headlineLarge = current.headlineLarge.copy(color = onBackground),
        headlineMedium = current.headlineMedium.copy(color = onBackground),
        headlineSmall = current.headlineSmall.copy(color = onBackground),
        bodyLarge = current.bodyLarge.copy(color = onBackground),
        bodyMedium = current.bodyMedium.copy(color = onBackground),
        bodySmall = current.bodySmall.copy(color = onBackground),
        labelLarge = current.labelLarge.copy(color = onBackground),
        labelMedium = current.labelMedium.copy(color = onBackground),
        labelSmall = current.labelSmall.copy(color = onBackground),
    )
}
