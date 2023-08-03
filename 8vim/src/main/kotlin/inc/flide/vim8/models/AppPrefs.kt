package inc.flide.vim8.models

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColorInt
import arrow.core.Either
import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import dev.patrickgold.jetpref.datastore.model.observeAsState
import inc.flide.vim8.models.error.LayoutError
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette
import kotlin.math.roundToInt

fun appPreferenceModel() = JetPref.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)
class AppPrefs : PreferenceModel("vim8-app-prefs") {
    val layout = Layout()
    val theme = Theme()
    val keyboard = Keyboard()
    val inputFeedback = InputFeedback()

    inner class Layout {
        val current = custom(
            key = "language_layout__custom",
            default = EmbeddedLayout("en"),
            serializer = LayoutSerializer
        )
        val custom = Custom()

        inner class Custom {
            val history = custom(
                key = "language_layout__custom_history",
                default = emptyList(),
                serializer = object : PreferenceSerializer<List<String>> {
                    override fun deserialize(value: String): List<String> {
                        return value.split('|')
                    }

                    override fun serialize(value: List<String>): String {
                        return value.joinToString("|")
                    }
                }
            )
        }

        @Composable
        fun keyboardData(): Either<LayoutError, KeyboardData> {
            val context = LocalContext.current
            val currentLayout by current.observeAsState()
            return currentLayout.loadKeyboardData(context = context)
        }
    }

    inner class InputFeedback {
        val soundEnabled = boolean(
            key = "input_feedback__sound_enabled",
            default = true,
        )
        val hapticEnabled = boolean(
            key = "input_feedback__haptic_enabled",
            default = true,
        )
    }

    val internal = Internal()

    inner class Theme {
        val mode = enum(
            key = "theme__mode",
            default = ThemeMode.SYSTEM
        )

        @Composable
        fun colorScheme(): ColorScheme {
            val context = LocalContext.current
            val mode by this.mode.observeAsState()
            return when (mode) {
                ThemeMode.LIGHT -> lightColorPalette(context)
                ThemeMode.DARK -> darkColorPalette(context)
                else -> when {
                    isSystemInDarkTheme() -> darkColorPalette(context)
                    else -> lightColorPalette(context)
                }
            }
        }
    }

    inner class Keyboard {
        val isSidebarOnLeft = boolean(
            key = "keyboard__is_sidebar_on_left",
            default = true
        )
        val customColors = CustomColors()
        val trail = Trail()
        val display = Display()
        val circle = Circle()

        inner class Circle {
            val radiusSizeFactor = int(
                key = "keyboard__circle__radius_size_factor",
                default = 12
            )
            val xCentreOffset = int(
                key = "keyboard__circle__x_centre_offset",
                default = 0
            )
            val yCentreOffset = int(
                key = "keyboard__circle__y_centre_offset",
                default = 0
            )
        }

        inner class CustomColors {
            val background = custom(
                key = "keyboard__custom_colors__background",
                default = Color.White,
                serializer = ColorPreferenceSerializer
            )
            val foreground = custom(
                key = "keyboard__custom_colors__foreground",
                default = Color.Black,
                serializer = ColorPreferenceSerializer
            )

        }

        @Composable
        fun colorScheme(): ColorScheme {
            val mode by theme.mode.observeAsState()
            val background by customColors.background.observeAsState()
            val foreground by customColors.foreground.observeAsState()
            val colorScheme = theme.colorScheme()
            return if (mode == ThemeMode.CUSTOM) colorScheme.copy(
                onSurface = foreground,
                surface = background
            ) else colorScheme
        }

        inner class Display {
            val showSectorIcons = boolean(
                key = "keyboard__display__show_sector_icons",
                default = true
            )
            val showLettersOnWheel = boolean(
                key = "keyboard__display__show_letters_on_wheel",
                default = true
            )
        }

        inner class Trail {
            val isVisible = boolean(
                key = "keyboard__trail__visibility",
                default = true
            )
            val useRandomColor = boolean(
                key = "keyboard__trail__use_random_color",
                default = true
            )
            val color = custom(
                "keyboard__trail__color",
                default = Color(61, 90, 254),
                serializer = ColorPreferenceSerializer
            )
        }
    }

    inner class Internal {
        val isImeSetup = boolean(key = "internal__is_ime_set_up", default = false)
    }
}

object ColorPreferenceSerializer : PreferenceSerializer<Color> {
    override fun deserialize(value: String): Color? {
        return try {
            Color(value.toColorInt())
        } catch (_: Exception) {
            null
        }
    }

    override fun serialize(value: Color): String? {
        return "#${value.alpha.toStringComponent()}${value.red.toStringComponent()}${value.green.toStringComponent()}${value.green.toStringComponent()}"
    }

    private fun Float.toStringComponent(): String =
        (this * 255).roundToInt().toString(16).let { if (it.length == 1) "0${it}" else it }
}