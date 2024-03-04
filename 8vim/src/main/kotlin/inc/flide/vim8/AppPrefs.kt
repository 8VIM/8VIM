package inc.flide.vim8

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.datastore.Datastore
import inc.flide.vim8.datastore.model.PreferenceMigrationEntry
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.layout.EmbeddedLayout
import inc.flide.vim8.ime.layout.LayoutSerDe
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette

fun appPreferenceModel() = Datastore.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)

class AppPrefs : PreferenceModel(4) {
    val layout = Layout()
    val theme = Theme()
    val clipboard = Clipboard()
    val keyboard = Keyboard()
    val inputFeedback = InputFeedback()
    val internal = Internal()

    inner class Clipboard {
        val history = stringSet(
            key = "prefs_clipboard_history",
            default = HashSet()
        )
        val enabled = boolean(
            key = "prefs_clipboard_enabled",
            default = true
        )
    }

    inner class Layout {
        val custom = Custom()

        val cache = stringSet(
            key = "prefs_layout_cache",
            default = emptySet()
        )

        val current = custom(
            key = "prefs_layout_current",
            default = EmbeddedLayout("en"),
            serde = LayoutSerDe
        )

        inner class Custom {
            val history = stringSet(
                key = "prefs_layout_custom_history",
                default = emptySet()
            )
        }
    }

    inner class InputFeedback {
        val soundEnabled = boolean(
            key = "prefs_input_feedback_sound_enabled",
            default = true
        )
        val hapticEnabled = boolean(
            key = "prefs_input_feedback_haptic_enabled",
            default = true
        )
    }

    inner class Theme {
        val mode = enum(
            key = "prefs_theme_color_mode",
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
        val sidebar = SideBar()
        val customColors = CustomColors()
        val trail = Trail()
        val display = Display()
        val circle = Circle()
        val behavior = Behavior()

        val height = int(
            key = "prefs_keyboard_height",
            default = 100
        )

        val emoticonKeyboard = string(
            key = "prefs_keyboard_emoticon_keyboard",
            default = ""
        )

        inner class SideBar {
            val isVisible = boolean(
                key = "prefs_keyboard_sidebar_is_visible",
                default = true
            )
            val isOnLeft = boolean(
                key = "prefs_keyboard_sidebar_is_on_left",
                default = true
            )
        }

        inner class Circle {
            val radiusSizeFactor = int(
                key = "prefs_keyboard_circle_radius_size_factor",
                default = 12
            )
            val xCentreOffset = int(
                key = "prefs_keyboard_circle_centre_x_offset",
                default = 0
            )
            val yCentreOffset = int(
                key = "prefs_keyboard_circle_centre_y_offset",
                default = 0
            )
        }

        inner class CustomColors {
            val background = int(
                key = "prefs_keyboard_custom_colors_background",
                default = Color(0xFA, 0xFA, 0xFA).toArgb()
            )
            val foreground = int(
                key = "prefs_keyboard_custom_colors_foreground",
                default = Color(21, 21, 21).toArgb()
            )
        }

        inner class Display {
            val showSectorIcons = boolean(
                key = "prefs_keyboard_display_show_sector_icons",
                default = true
            )
            val showLettersOnWheel = boolean(
                key = "prefs_keyboard_display_show_letters_on_wheel",
                default = true
            )
        }

        inner class Trail {
            val isVisible = boolean(
                key = "prefs_keyboard_trail_is_visible",
                default = true
            )
            val useRandomColor = boolean(
                key = "prefs_keyboard_trail_use_random_color",
                default = true
            )
            val color = int(
                "prefs_keyboard_trail_color",
                default = Color(0x3D, 0x5A, 0xFE).toArgb()
            )
        }

        inner class Behavior {
            val cursor = Cursor()

            inner class Cursor {
                val moveByWord =
                    boolean(key = "prefs_keyboard_behavior_cursor_move_by_word", default = false)
            }
        }
    }

    inner class Internal {
        val isImeSetup = boolean(key = "internal__is_ime_set_up", default = false)
        val versionCode = int(key = "internal__versionCode", default = BuildConfig.VERSION_CODE)
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

            1 -> when (entry.key) {
                "clipboard_history" -> entry.transform(key = "prefs_clipboard_history")
                "user_preferred_clipboard_enabled" -> entry.transform(
                    key = "prefs_clipboard_enabled"
                )

                "select_keyboard_layout" -> entry.transform(key = "prefs_layout_current")
                "pref_custom_keyboard_layout_history" -> entry.transform(
                    key = "prefs_layout_custom_history"
                )

                "user_preferred_sound_feedback_enabled" -> entry.transform(
                    key = "prefs_input_feedback_sound_enabled"
                )

                "user_preferred_haptic_feedback_enabled" -> entry.transform(
                    key = "prefs_input_feedback_haptic_enabled"
                )

                "color_mode" -> entry.transform(key = "prefs_theme_color_mode")
                "x_board_keyboard_height" -> entry.transform(key = "prefs_keyboard_height")
                "selected_emoticon_keyboard" -> entry.transform(
                    key = "prefs_keyboard_emoticon_keyboard"
                )

                "user_preferred_sidebar_visibility" -> entry.transform(
                    key = "prefs_keyboard_sidebar_is_visible"
                )

                "user_preferred_sidebar_left" -> entry.transform(
                    key = "prefs_keyboard_sidebar_is_on_left"
                )

                "x_board_circle_radius_size_factor" -> entry.transform(
                    key = "prefs_keyboard_sidebar_circle_radius_size_factor"
                )

                "x_board_circle_centre_x_offset" -> entry.transform(
                    key = "prefs_keyboard_sidebar_circle_centre_x_offset"
                )

                "x_board_circle_centre_y_offset" -> entry.transform(
                    key = "prefs_keyboard_sidebar_circle_centre_y_offset"
                )

                "board_bg_color" -> entry.transform(key = "prefs_keyboard_custom_colors_background")
                "board_fg_color" -> entry.transform(key = "prefs_keyboard_custom_colors_foreground")
                "user_preferred_display_icons_in" -> entry.transform(
                    key = "prefs_keyboard_display_show_sector_icons"
                )

                "user_preferred_display_letters_on_wheel" -> entry.transform(
                    key = "prefs_keyboard_display_show_letters_on_wheel"
                )

                "user_preferred_typing_trail_visibility" -> entry.transform(
                    key = "prefs_keyboard_trail_is_visible"
                )

                "user_preferred_random_trail_color_enabled" -> entry.transform(
                    key = "prefs_keyboard_trail_use_random_color"
                )

                "trail_color" -> entry.transform(key = "prefs_keyboard_trail_color")
                else -> entry.keepAsIs()
            }

            3 -> when (entry.key) {
                "prefs_keyboard_sidebar_circle_radius_size_factor" -> entry.transform(
                    key = "prefs_keyboard_circle_radius_size_factor"
                )
                "prefs_keyboard_sidebar_circle_centre_x_offset" -> entry.transform(
                    key = "prefs_keyboard_circle_centre_x_offset"
                )
                "prefs_keyboard_sidebar_circle_centre_y_offset" -> entry.transform(
                    key = "prefs_keyboard_circle_centre_y_offset"
                )
                else -> entry.keepAsIs()
            }

            else -> entry.keepAsIs()
        }
    }

    override fun postInitialize(context: Context) {
        if (internal.versionCode.get() != BuildConfig.VERSION_CODE) {
            layout.cache.reset()
            context.cacheDir.deleteRecursively()
        }
        internal.versionCode.set(BuildConfig.VERSION_CODE)
    }
}
