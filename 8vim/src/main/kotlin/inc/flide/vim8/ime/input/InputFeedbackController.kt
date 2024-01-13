package inc.flide.vim8.ime.input

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import androidx.compose.runtime.staticCompositionLocalOf
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.systemServiceOrNull

val LocalInputFeedbackController =
    staticCompositionLocalOf<InputFeedbackController> { error("not init") }

class InputFeedbackController private constructor(private val ims: InputMethodService) {
    companion object {
        fun new(ims: InputMethodService) = InputFeedbackController(ims)
    }

    private val prefs by appPreferenceModel()

    private val audioManager = ims.systemServiceOrNull(AudioManager::class)

    fun keyPress(keySound: Int = 0) {
        if (prefs.inputFeedback.hapticEnabled.get()) performHapticFeedback()
        if (prefs.inputFeedback.soundEnabled.get()) performAudioFeedback(keySound(keySound))
    }

    private fun keySound(keyCode: Int): Int {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> AudioManager.FX_KEYPRESS_DELETE
            KeyEvent.KEYCODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
    }

    @Suppress("DEPRECATION")
    private fun performHapticFeedback() {
        val view = ims.window?.window?.decorView ?: return
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private fun performAudioFeedback(keySound: Int) {
        audioManager?.playSoundEffect(keySound)
    }
}
