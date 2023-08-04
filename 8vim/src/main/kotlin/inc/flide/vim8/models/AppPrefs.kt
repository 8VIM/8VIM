package inc.flide.vim8.models

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import arrow.core.Either
import inc.flide.vim8.datastore.Datastore
import inc.flide.vim8.datastore.model.PreferenceMigrationEntry
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.models.error.LayoutError
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette

fun appPreferenceModel() = Datastore.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)
class AppPrefs : PreferenceModel(1) {
    val layout = Layout()
    val theme = Theme()
    val cliboard = Clipboard()

    inner class Clipboard {
        val history = stringSet(
            key = "clipboard_history",
            default = HashSet()
        )
    }

    val keyboard = Keyboard()
    val inputFeedback = InputFeedback()

    inner class Layout {
        val current = custom(
            key = "select_keyboard_layout",
            default = EmbeddedLayout("en"),
            serde = LayoutSerDe
        )
        val custom = Custom()

        inner class Custom {
            val history = stringSet(
                key = "pref_custom_keyboard_layout_history",
                default = emptySet(),
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
            key = "user_preferred_haptic_feedback_enabled",
            default = true,
        )
        val hapticEnabled = boolean(
            key = "user_preferred_sound_feedback_enabled",
            default = true,
        )
    }

    val internal = Internal()

    inner class Theme {
        val mode = enum(
            key = "color_mode",
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
            key = "user_preferred_sidebar_left",
            default = true
        )
        val customColors = CustomColors()
        val trail = Trail()
        val display = Display()
        val circle = Circle()

        inner class Circle {
            val radiusSizeFactor = int(
                key = "pref_circle_scale_factor",
                default = 12
            )
            val xCentreOffset = int(
                key = "x_board_circle_centre_x_offset",
                default = 0
            )
            val yCentreOffset = int(
                key = "x_board_circle_centre_y_offset",
                default = 0
            )
        }

        inner class CustomColors {
            val background = int(
                key = "keyboard__custom_colors__background",
                default = Color(0xFA, 0xFA, 0xFA).toArgb(),
            )
            val foreground = int(
                key = "keyboard__custom_colors__foreground",
                default = Color(21, 21, 21).toArgb(),
            )

        }

        @Composable
        fun colorScheme(): ColorScheme {
            val mode by theme.mode.observeAsState()
            val background by customColors.background.observeAsState()
            val foreground by customColors.foreground.observeAsState()
            val colorScheme = theme.colorScheme()
            return if (mode == ThemeMode.CUSTOM) colorScheme.copy(
                onSurface = Color(foreground),
                surface = Color(background)
            ) else colorScheme
        }

        inner class Display {
            val showSectorIcons = boolean(
                key = "user_preferred_display_icons_in",
                default = true
            )
            val showLettersOnWheel = boolean(
                key = "user_preferred_display_letters_on_wheel",
                default = true
            )
        }

        inner class Trail {
            val isVisible = boolean(
                key = "user_preferred_typing_trail_visibility",
                default = true
            )
            val useRandomColor = boolean(
                key = "user_preferred_random_trail_color_enabled",
                default = true
            )
            val color = int(
                "trail_color",
                default = Color(0x3D, 0x5A, 0xFE).toArgb()
            )
        }
    }

    inner class Internal {
        val isImeSetup = boolean(key = "internal__is_ime_set_up", default = false)
    }

    override fun migrate(
        previousVersion: Int,
        entry: PreferenceMigrationEntry
    ): PreferenceMigrationEntry {
        return when (previousVersion) {
            0 -> when (entry.key) {
                "color_mode" -> entry.transform(rawValue = entry.rawValue.toString().uppercase())
                else -> entry.keepAsIs()
            }

            else -> entry.keepAsIs()
        }
    }
}