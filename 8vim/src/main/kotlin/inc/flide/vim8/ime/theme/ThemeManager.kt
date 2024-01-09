package inc.flide.vim8.ime.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.isDarkTheme
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette
import kotlin.random.Random

class ThemeManager(context: Context) {
    private val prefs: AppPrefs by appPreferenceModel()
    private val randomTrailColor = RandomTrailColor()
    private val darkColorScheme: ColorScheme = darkColorPalette(context)
    private val lightColorScheme: ColorScheme = lightColorPalette(context)

    var configuration: Configuration = context.resources.configuration
        set(value) {
            field = value
            updateCurrentTheme()
        }
    private val _currentTheme = MutableLiveData(themeInfo())
    val currentTheme: LiveData<ThemeInfo> get() = _currentTheme

    init {
        val customColors = prefs.keyboard.customColors
        val trailColor = prefs.keyboard.trail
        prefs.theme.mode.observe {
            updateCurrentTheme()
        }
        customColors.background.observe { updateCurrentTheme() }
        customColors.foreground.observe { updateCurrentTheme() }
        trailColor.useRandomColor.observe { updateCurrentTheme() }
        trailColor.color.observe { updateCurrentTheme() }

    }

    fun updateCurrentTheme() {
        _currentTheme.postValue(themeInfo())
    }

    private fun themeInfo(): ThemeInfo = ThemeInfo(colorScheme(), trailColor())

    private fun colorScheme(): ColorScheme {
        val mode = prefs.theme.mode.get()
        return when {
            mode == ThemeMode.CUSTOM -> {
                val customColorsPrefs = prefs.keyboard.customColors
                val backgroundColor = Color(customColorsPrefs.background.get())
                val foregroundColor = Color(customColorsPrefs.foreground.get())
                lightColorScheme.copy(
                    background = backgroundColor,
                    surface = backgroundColor,
                    onBackground = foregroundColor,
                    onSurface = foregroundColor
                )
            }

            mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && configuration.isDarkTheme()) -> darkColorScheme
            else -> lightColorScheme
        }
    }

    private fun trailColor(): TrailColor =
        if (prefs.keyboard.trail.useRandomColor.get()) randomTrailColor
        else FixedTrailColor(prefs.keyboard.trail.color.get())

    data class ThemeInfo(val scheme: ColorScheme, val trailColor: TrailColor)

    interface TrailColor {
        fun color(): Int
    }

    class FixedTrailColor(private val color: Int) : TrailColor {
        override fun color(): Int = color
    }

    class RandomTrailColor : TrailColor {
        override fun color(): Int = Color(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256),
            255
        ).toArgb()
    }
}