package inc.flide.vim8.ime.actionlisteners

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.layout.models.CustomKeycode.Companion.KEY_CODE_TO_STRING_CODE_MAP
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API29_Q

@Suppress("DEPRECATION")
abstract class KeypadActionListener(
    protected val vim8ImeService: Vim8ImeService,
    protected val view: View
) {
    private val prefs by appPreferenceModel()
    val ctrlState: Boolean
        get() = vim8ImeService.ctrlState
    val isPassword: Boolean
        get() = vim8ImeService.isPassword
    private val audioManager: AudioManager =
        view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun keyCodeIsValid(keyCode: Int): Boolean {
        val keycodeProfileSwitch =
            if (ATLEAST_API29_Q) KeyEvent.KEYCODE_PROFILE_SWITCH else KEYCODE_PROFILE_SWITCH
        return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= keycodeProfileSwitch
    }

    fun performCtrlToggle() {
        vim8ImeService.performCtrlToggle()
    }
    fun handleInputKey(keyCode: Int, keyFlags: Int) {
        val actionHandled =
            handleKeyEventKeyCodes(keyCode, keyFlags) || (
                KEY_CODE_TO_STRING_CODE_MAP[keyCode]?.handleKeyCode(
                    vim8ImeService
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
                KeyEvent.KEYCODE_CUT -> vim8ImeService.cut()
                KeyEvent.KEYCODE_COPY -> vim8ImeService.copy()
                KeyEvent.KEYCODE_PASTE -> vim8ImeService.paste()
                KeyEvent.KEYCODE_ENTER -> vim8ImeService.commitImeOptionsBasedEnter()
//                KeyEvent.KEYCODE_DEL -> vim8ImeService.delete()
                else -> {
                    val flags = if (isDPad(primaryCode)) {
                        keyFlags or vim8ImeService.ctrlFlag
                    } else {
                        keyFlags
                    }
                    vim8ImeService.sendKey(primaryCode, flags)
                    vim8ImeService.resetShiftState()
                }
            }
            return true
        }
        return false
    }

    private fun isDPad(primaryCode: Int): Boolean {
        return when (primaryCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                true
            }

            else -> false
        }
    }

    fun onText(text: CharSequence) {
        vim8ImeService.sendText(text.toString())
        vim8ImeService.resetShiftState()
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD)
    }

    fun handleInputText(keyboardAction: KeyboardAction) {
        val isUpperCase = vim8ImeService.shiftState != Vim8ImeService.State.OFF
        val text =
            if (isUpperCase && keyboardAction.capsLockText.isNotEmpty()) {
                keyboardAction.capsLockText
            } else {
                keyboardAction.text
            }
        onText(text)
    }

    fun areCharactersCapitalized(): Boolean {
        return vim8ImeService.areCharactersCapitalized()
    }

    val isShiftSet: Boolean
        get() = vim8ImeService.shiftLockFlag == KeyEvent.META_SHIFT_ON
    val isCapsLockSet: Boolean
        get() = vim8ImeService.capsLockFlag == KeyEvent.META_CAPS_LOCK_ON

    open fun findLayer(): LayerLevel {
        return LayerLevel.FIRST
    }

    companion object {
        const val KEYCODE_PROFILE_SWITCH = 288
    }
}
