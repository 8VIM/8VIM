package inc.flide.vim8.ime.input

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Vibrator
import android.view.KeyEvent
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.lib.android.systemServiceOrNull
import inc.flide.vim8.lib.android.systemVibratorOrNull
import inc.flide.vim8.lib.android.vibrate
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify

class InputFeedbackControllerSpec : FunSpec({
    val keyCodes = mapOf(
        KeyEvent.KEYCODE_ENTER to "KEYCODE_ENTER",
        KeyEvent.KEYCODE_DEL to "KEYCODE_DEL",
        KeyEvent.KEYCODE_FORWARD_DEL to "KEYCODE_FORWARD_DEL",
        KeyEvent.KEYCODE_SPACE to "KEYCODE_SPACE",
        KeyEvent.KEYCODE_A to "KEYCODE_A"
    )

    val soundFxs = mapOf(
        AudioManager.FX_KEYPRESS_RETURN to "FX_KEYPRESS_RETURN",
        AudioManager.FX_KEYPRESS_DELETE to "FX_KEYPRESS_DELETE",
        AudioManager.FX_KEYPRESS_SPACEBAR to "FX_KEYPRESS_SPACEBAR",
        AudioManager.FX_KEYPRESS_STANDARD to "FX_KEYPRESS_STANDARD"
    )
    val values = listOf(true, false)

    val sounds = listOf(
        KeyEvent.KEYCODE_ENTER to AudioManager.FX_KEYPRESS_RETURN,
        KeyEvent.KEYCODE_DEL to AudioManager.FX_KEYPRESS_DELETE,
        KeyEvent.KEYCODE_FORWARD_DEL to AudioManager.FX_KEYPRESS_DELETE,
        KeyEvent.KEYCODE_SPACE to AudioManager.FX_KEYPRESS_SPACEBAR,
        KeyEvent.KEYCODE_A to AudioManager.FX_KEYPRESS_STANDARD
    )

    val ims = mockk<InputMethodService>(relaxed = true)
    val audioManager = mockk<AudioManager>(relaxed = true)
    val vibrator = mockk<Vibrator>(relaxed = true)
    val hapticEnabledPref = mockk<PreferenceData<Boolean>>(relaxed = true)
    val hapticSectorCrossPref = mockk<PreferenceData<Boolean>>(relaxed = true)
    val soundEnabledPref = mockk<PreferenceData<Boolean>>(relaxed = true)
    val soundSectorCrossEnabledPref = mockk<PreferenceData<Boolean>>(relaxed = true)

    beforeSpec {
        mockkStatic(Context::class)
        mockkStatic(Context::systemVibratorOrNull)
        mockkStatic(::appPreferenceModel)
        val prefs = mockk<AppPrefs>()
        val inputFeedbackPref = mockk<AppPrefs.InputFeedback>()
        val soundVolume = mockk<PreferenceData<Int>>()
        every { ims.systemVibratorOrNull() } returns vibrator
        every { ims.systemServiceOrNull(AudioManager::class) } returns audioManager
        every { prefs.inputFeedback } returns inputFeedbackPref
        every { inputFeedbackPref.hapticEnabled } returns hapticEnabledPref
        every { inputFeedbackPref.hapticSectorCrossEnabled } returns hapticSectorCrossPref
        every { inputFeedbackPref.soundEnabled } returns soundEnabledPref
        every { inputFeedbackPref.soundSectorCrossEnabled } returns soundSectorCrossEnabledPref
        every { inputFeedbackPref.soundVolume } returns soundVolume
        every { soundVolume.get() } returns 100
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
    }

    beforeTest {
        clearMocks(
            hapticEnabledPref,
            hapticSectorCrossPref,
            soundEnabledPref,
            soundSectorCrossEnabledPref,
            vibrator,
            audioManager
        )
    }

    context("keyPress") {
        withData(nameFn = { "Haptic: $it" }, values) { haptic ->
            withData(nameFn = { "Sound: $it" }, values) { sound ->
                withData(nameFn = { "Repeat: $it" }, values) { repeat ->
                    withData(
                        nameFn = {
                            "Keycode: ${keyCodes[it.first]} produces ${soundFxs[it.second]}"
                        },
                        sounds
                    ) { (keyCode, soundFx) ->
                        every { hapticEnabledPref.get() } returns haptic
                        every { soundEnabledPref.get() } returns sound
                        val controller = InputFeedbackController.new(ims)
                        controller.keyPress(keyCode, repeat)
                        val volume = if (repeat) 0.4f else 1f
                        val factor = if (repeat) 0.05 else 1.0
                        if (sound) {
                            verify { audioManager.playSoundEffect(soundFx, volume) }
                        }
                        if (haptic) {
                            verify {
                                vibrator.vibrate(
                                    duration = 50,
                                    strength = 50,
                                    factor = factor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    context("sectorCross") {
        withData(nameFn = { "Haptic: $it" }, values) { haptic ->
            withData(nameFn = { "Haptic sector cross: $it" }, values) { hapticSector ->
                withData(nameFn = { "Sound: $it" }, values) { sound ->
                    withData(nameFn = { "Sound sector cross: $it" }, values) { soundSector ->
                        every { hapticEnabledPref.get() } returns haptic
                        every { hapticSectorCrossPref.get() } returns hapticSector
                        every { soundEnabledPref.get() } returns sound
                        every { soundSectorCrossEnabledPref.get() } returns soundSector
                        val controller = InputFeedbackController.new(ims)
                        controller.sectorCross()
                        if (sound && soundSector) {
                            verify {
                                audioManager.playSoundEffect(
                                    AudioManager.FX_KEYPRESS_STANDARD,
                                    0.5f
                                )
                            }
                        }
                        if (haptic && hapticSector) {
                            verify {
                                vibrator.vibrate(
                                    duration = 50,
                                    strength = 50,
                                    factor = 0.4
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    afterSpec {
        clearStaticMockk(Context::class)
    }
})
