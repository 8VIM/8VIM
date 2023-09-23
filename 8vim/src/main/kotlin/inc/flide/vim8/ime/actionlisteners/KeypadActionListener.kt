package inc.flide.vim8.ime.actionlisteners

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API29_Q
import inc.flide.vim8.models.CustomKeycode.Companion.KEY_CODE_TO_STRING_CODE_MAP
import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.LayerLevel
import inc.flide.vim8.models.appPreferenceModel

@Suppress("DEPRECATION")
abstract class KeypadActionListener(
    protected val mainInputMethodService: MainInputMethodService,
    protected val view: View
) {
    private val prefs by appPreferenceModel()
    private val audioManager: AudioManager =
        view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun keyCodeIsValid(keyCode: Int): Boolean {
        val keycodeProfileSwitch =
            if (ATLEAST_API29_Q) KeyEvent.KEYCODE_PROFILE_SWITCH else KEYCODE_PROFILE_SWITCH
        return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= keycodeProfileSwitch
    }

    fun handleInputKey(keyCode: Int, keyFlags: Int) {
        val actionHandled =
            handleKeyEventKeyCodes(keyCode, keyFlags) || (
                KEY_CODE_TO_STRING_CODE_MAP[keyCode]?.handleKeyCode(
                    mainInputMethodService
                ) ?: true
                )
        if (!actionHandled) {
            onText(keyCode.toChar().toString())
        } else {
            performInputAcceptedFeedback(keySound(keyCode))
        }
    }

    private fun keySound(keyCode: Int): Int {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> AudioManager.FX_KEYPRESS_DELETE
            KeyEvent.KEYCODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
    }

    private fun performInputAcceptedFeedback(keySound: Int) {
        if (prefs.inputFeedback.hapticEnabled.get()) {
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
        if (prefs.inputFeedback.soundEnabled.get()) {
            audioManager.playSoundEffect(keySound)
        }
    }

    private fun handleKeyEventKeyCodes(primaryCode: Int, keyFlags: Int): Boolean {
        if (keyCodeIsValid(primaryCode)) {
            when (primaryCode) {
                KeyEvent.KEYCODE_CUT -> mainInputMethodService.cut()
                KeyEvent.KEYCODE_COPY -> mainInputMethodService.copy()
                KeyEvent.KEYCODE_PASTE -> mainInputMethodService.paste()
                KeyEvent.KEYCODE_ENTER -> mainInputMethodService.commitImeOptionsBasedEnter()
                KeyEvent.KEYCODE_DEL -> mainInputMethodService.delete()
                else -> {
                    mainInputMethodService.sendKey(primaryCode, keyFlags)
                    mainInputMethodService.shiftLockFlag = 0
                }
            }
            return true
        }
        return false
    }

    fun onText(text: CharSequence) {
        mainInputMethodService.sendText(text.toString())
        mainInputMethodService.shiftLockFlag = 0
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD)
    }

    fun handleInputText(keyboardAction: KeyboardAction) {
        val isUpperCase = isShiftSet || isCapsLockSet
        val text =
            if (isUpperCase && keyboardAction.capsLockText.isNotEmpty()) {
                keyboardAction.capsLockText
            } else {
                keyboardAction.text
            }
        onText(text)
    }

    fun areCharactersCapitalized(): Boolean {
        return mainInputMethodService.areCharactersCapitalized()
    }

    val isShiftSet: Boolean
        get() = mainInputMethodService.shiftLockFlag == KeyEvent.META_SHIFT_ON
    val isCapsLockSet: Boolean
        get() = mainInputMethodService.capsLockFlag == KeyEvent.META_CAPS_LOCK_ON

    open fun findLayer(): LayerLevel {
        return LayerLevel.FIRST
    }

    companion object {
        const val KEYCODE_PROFILE_SWITCH = 288
    }
}
