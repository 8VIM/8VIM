package inc.flide.vim8.app.settings

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.R
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.datastore.ui.RangeSliderPreference
import inc.flide.vim8.datastore.ui.SliderPreference
import inc.flide.vim8.datastore.ui.SwitchPreference
import inc.flide.vim8.lib.compose.Dialog
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.lib.util.InputMethodUtils.listOtherKeyboard

@Composable
fun KeyboardScreen() = Screen {
    title = stringRes(R.string.settings__keyboard__title)
    previewFieldVisible = true
    val context = LocalContext.current
    content {
        val circleAutoResize by prefs.keyboard.circle.autoResize.observeAsState()
        val isDynamicCircleEnabled by prefs.keyboard.circle.dynamic.isEnabled.observeAsState()
        val hapticEnabled by prefs.inputFeedback.hapticEnabled.observeAsState()
        val soundEnabled by prefs.inputFeedback.soundEnabled.observeAsState()

        PreferenceGroup {
            Dialog {
                title = stringRes(R.string.select_preferred_emoticon_keyboard_dialog_title)
                index = {
                    selectedKeyboard(
                        prefs.keyboard.emoticonKeyboard,
                        context.applicationContext
                    )
                }
                items = { listOtherKeyboard(context.applicationContext).keys }
                onConfirm {
                    if (it == -1) return@onConfirm
                    prefs.keyboard.emoticonKeyboard.set(
                        listOtherKeyboard(context.applicationContext).values.toList()[it]
                    )
                }
                Preference(
                    title = stringRes(R.string.settings__keyboard__select__emoji__keyboard__title),
                    summary = stringRes(
                        R.string.settings__keyboard__select__emoji__keyboard__summary
                    ),
                    onClick = { show() },
                    trailing = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        PreferenceGroup(
            title = stringRes(R.string.settings__keyboard__circle__auto_resize_group__title)
        ) {
            SwitchPreference(
                prefs.keyboard.circle.autoResize,
                title = stringRes(R.string.settings__keyboard__circle__auto_resize__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__circle__auto_resize__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__circle__auto_resize__summary__on
                )
            )

            if (circleAutoResize) {
                RangeSliderPreference(
                    minPref = prefs.keyboard.circle.radiusMinSizeFactor,
                    maxPref = prefs.keyboard.circle.radiusSizeFactor,
                    title = stringRes(R.string.settings__keyboard__circle__size__title),
                    summary = stringRes(R.string.settings__keyboard__circle__size__summary),
                    min = 1,
                    max = 40
                )
            } else {
                SliderPreference(
                    pref = prefs.keyboard.circle.radiusSizeFactor,
                    title = stringRes(R.string.settings__keyboard__circle__size__title),
                    summary = stringRes(R.string.settings__keyboard__circle__size__summary),
                    min = 1,
                    max = 40
                )
            }

            SwitchPreference(
                prefs.keyboard.circle.dynamic.isEnabled,
                title = stringRes(R.string.settings__keyboard__circle__dynamic_centre__title),
                summary = stringRes(R.string.settings__keyboard__circle__dynamic_centre__summary)
            )

            SwitchPreference(
                prefs.keyboard.circle.dynamic.isOverlayEnabled,
                title = stringRes(
                    R.string.settings__keyboard__circle__dynamic_centre_overlay_enabled__title
                ),
                visibleIf = { isDynamicCircleEnabled }
            )
        }

        PreferenceGroup(
            title = stringRes(R.string.settings__keyboard__circle__offset_and_height_group__title)
        ) {
            SliderPreference(
                pref = prefs.keyboard.circle.xCentreOffset,
                title = stringRes(R.string.settings__keyboard__circle__x__centre__offset__title),
                min = -5,
                max = 5
            )
            SliderPreference(
                pref = prefs.keyboard.circle.yCentreOffset,
                title = stringRes(R.string.settings__keyboard__circle__y__centre__offset__title),
                min = -5,
                max = 5
            )
            SliderPreference(
                pref = prefs.keyboard.height,
                title = stringRes(R.string.settings__keyboard__height__title),
                summary = stringRes(R.string.settings__keyboard__height__summary),
                min = 1,
                max = 200
            )
        }

        PreferenceGroup(
            title = stringRes(R.string.settings__keyboard__display__group__title)
        ) {
            SwitchPreference(
                pref = prefs.keyboard.display.showSectorIcons,
                title = stringRes(R.string.settings__keyboard__display__show__sector__icons__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__display__show__sector__icons__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__display__show__sector__icons__summary__on
                )
            )
            SwitchPreference(
                pref = prefs.keyboard.display.showLettersOnWheel,
                title = stringRes(
                    R.string.settings__keyboard__display__show__letters__on__wheel__title
                ),
                summaryOff = stringRes(
                    R.string.settings__keyboard__display__show__letters__on__wheel__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__display__show__letters__on__wheel__summary__on
                )
            )
        }

        PreferenceGroup(
            title = stringRes(R.string.settings__keyboard__sidebar__group__title)
        ) {
            SwitchPreference(
                pref = prefs.keyboard.sidebar.isVisible,
                title = stringRes(R.string.settings__keyboard__sidebar__is__visible__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__sidebar__is__visible__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__sidebar__is__visible__summary__on
                )
            )
            SwitchPreference(
                pref = prefs.keyboard.sidebar.isOnLeft,
                title = stringRes(
                    R.string.settings__keyboard__sidebar__is__on__left__title
                ),
                summaryOff = stringRes(
                    R.string.settings__keyboard__sidebar__is__on__left__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__sidebar__is__on__left__summary__on
                )
            )
        }

        PreferenceGroup(
            title = stringRes(R.string.settings__keyboard__haptic__sound__group__title)
        ) {
            SwitchPreference(
                pref = prefs.inputFeedback.hapticEnabled,
                title = stringRes(R.string.settings__keyboard__haptic__feedback__enabled__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__haptic__feedback__enabled__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__haptic__feedback__enabled__summary__on
                )
            )
            SwitchPreference(
                pref = prefs.inputFeedback.hapticSectorCrossEnabled,
                title = @Suppress("ktlint")
                stringRes(
                    R.string.settings__keyboard__haptic__feedback__haptic_sector_cross_enabled__title
                ),
                summaryOff = stringRes(
                    R.string.settings__keyboard__haptic__feedback__enabled__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__haptic__feedback__enabled__summary__on
                ),
                visibleIf = { hapticEnabled }
            )
            SwitchPreference(
                pref = prefs.inputFeedback.soundEnabled,
                title = stringRes(R.string.settings__keyboard__sound__feedback__enabled__title),
                summaryOff = stringRes(
                    R.string.settings__keyboard__sound__feedback__enabled__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__sound__feedback__enabled__summary__on
                )
            )
            SliderPreference(
                pref = prefs.inputFeedback.soundVolume,
                title = stringRes(R.string.settings__keyboard__sound__feedback__volume__title),
                min = 0,
                max = 100,
                visibleIf = { soundEnabled },
                toText = {
                    if (it == 0) {
                        "System default"
                    } else {
                        "$it%"
                    }
                }
            )
            SwitchPreference(
                pref = prefs.inputFeedback.soundSectorCrossEnabled,
                title = stringRes(
                    R.string.settings__keyboard__haptic__feedback__sound_sector_cross_enabled__title
                ),
                summaryOff = stringRes(
                    R.string.settings__keyboard__sound__feedback__enabled__summary__off
                ),
                summaryOn = stringRes(
                    R.string.settings__keyboard__sound__feedback__enabled__summary__on
                ),
                visibleIf = { soundEnabled }
            )
        }
    }
}

private fun selectedKeyboard(pref: PreferenceData<String>, context: Context): Int {
    val index = listOtherKeyboard(context).values.indexOf(pref.get())
    if (index == -1) {
        pref.reset()
    }
    return index
}
