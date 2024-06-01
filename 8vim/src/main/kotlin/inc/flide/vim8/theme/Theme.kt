package inc.flide.vim8.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.luminance
import inc.flide.vim8.lib.android.AndroidVersion

fun darkColorPalette(context: Context) = if (AndroidVersion.ATLEAST_API31_S) {
    dynamicDarkColorScheme(context)
} else {
    darkColorScheme()
}

fun lightColorPalette(context: Context) = if (AndroidVersion.ATLEAST_API31_S) {
    dynamicLightColorScheme(context)
} else {
    lightColorScheme()
}

fun ColorScheme.isLightTheme() = this.background.luminance() >= 0.5f

fun ColorScheme.typography(): Typography {
    val current = Typography()
    return current.copy(
        displayLarge = current.displayLarge.copy(color = onSurface),
        displayMedium = current.displayMedium.copy(color = onSurface),
        displaySmall = current.displaySmall.copy(color = onSurface),
        titleLarge = current.titleLarge.copy(color = onSurface),
        titleMedium = current.titleMedium.copy(color = onSurface),
        titleSmall = current.titleSmall.copy(color = onSurface),
        headlineLarge = current.headlineLarge.copy(color = onSurface),
        headlineMedium = current.headlineMedium.copy(color = onSurface),
        headlineSmall = current.headlineSmall.copy(color = onSurface),
        bodyLarge = current.bodyLarge.copy(color = onSurface),
        bodyMedium = current.bodyMedium.copy(color = onSurface),
        bodySmall = current.bodySmall.copy(color = onSurface),
        labelLarge = current.labelLarge.copy(color = onSurface),
        labelMedium = current.labelMedium.copy(color = onSurface),
        labelSmall = current.labelSmall.copy(color = onSurface)
    )
}
