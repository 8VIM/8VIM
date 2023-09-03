package inc.flide.vim8.ime

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette
import kotlin.properties.Delegates
import kotlin.random.Random

class KeyboardTheme internal constructor(context: Context) {
    private val prefs: AppPrefs by appPreferenceModel()
    private val darkColorScheme: ColorScheme
    private val lightColorScheme: ColorScheme
    private val onChangeCallbacks: MutableList<OnChangeCallback> = ArrayList()
    var backgroundColor by Delegates.notNull<Int>()
        private set
    var foregroundColor by Delegates.notNull<Int>()
        private set
    val trailColor: Int
        get() = if (!prefs.keyboard.trail.useRandomColor.get()) {
            prefs.keyboard.trail.color.get()
        } else {
            Color(
                Random.nextInt(256),
                Random.nextInt(256),
                Random.nextInt(256),
                255
            ).toArgb()
        }
    var configuration: Configuration = context.resources.configuration
        set(value) {
            field = value
            if (prefs.theme.mode.get() == ThemeMode.SYSTEM) {
                onPrefChanged()
            }
        }

    init {
        darkColorScheme = darkColorPalette(context)
        lightColorScheme = lightColorPalette(context)
        updateColors()
        val customColors = prefs.keyboard.customColors
        prefs.theme.mode.observe { onPrefChanged() }
        customColors.background.observe { onPrefChanged() }
        customColors.foreground.observe { onPrefChanged() }
    }

    fun onChange(onChangeCallback: OnChangeCallback) {
        onChangeCallbacks.add(onChangeCallback)
    }

    private fun onPrefChanged() {
        updateColors()
        onChangeCallbacks.forEach { it.invoke() }
    }

    private fun updateColors() {
        when (val mode = prefs.theme.mode.get()) {
            ThemeMode.CUSTOM -> {
                val customColorsPrefs = prefs.keyboard.customColors
                backgroundColor = customColorsPrefs.background.get()
                foregroundColor = customColorsPrefs.foreground.get()
            }

            else -> {
                val colorScheme = when (mode) {
                    ThemeMode.LIGHT -> lightColorScheme
                    ThemeMode.DARK -> darkColorScheme
                    else -> when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> darkColorScheme
                        else -> lightColorScheme
                    }
                }
                backgroundColor = colorScheme.background.toArgb()
                foregroundColor = colorScheme.onBackground.toArgb()
            }
        }
    }

    interface OnChangeCallback {
        operator fun invoke()
    }

    companion object {
        private var singleton: KeyboardTheme? = null

        fun initialize(context: Context) {
            if (singleton == null) {
                singleton = KeyboardTheme(context)
            }
        }

        @JvmStatic
        fun getInstance(): KeyboardTheme {
            return singleton!!
        }
    }
}
