package inc.flide.vim8.ime.actionlisteners

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.layout.models.CustomKeycode.Companion.KEY_CODE_TO_STRING_CODE_MAP
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API29_Q

@Suppress("DEPRECATION")
abstract class KeypadActionListener(
    protected val mainInputMethodService: MainInputMethodService,
    protected val view: View
) {
    private val prefs by appPreferenceModel()
    val ctrlState: Boolean
        get() = mainInputMethodService.ctrlState
    private val audioManager: AudioManager =
        view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun keyCodeIsValid(keyCode: Int): Boolean {
        val keycodeProfileSwitch =
            if (ATLEAST_API29_Q) KeyEvent.KEYCODE_PROFILE_SWITCH else KEYCODE_PROFILE_SWITCH
        return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= keycodeProfileSwitch
    }

    fun performCtrlToggle() {
        mainInputMethodService.performCtrlToggle()
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
                    val flags = if (isDPad(primaryCode)) {
                        keyFlags or mainInputMethodService.ctrlFlag
                    } else {
                        keyFlags
                    }
                    mainInputMethodService.sendKey(primaryCode, flags)
                    mainInputMethodService.resetShiftState()
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
        mainInputMethodService.sendText(text.toString())
        mainInputMethodService.resetShiftState()
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD)
    }

    fun handleInputText(keyboardAction: KeyboardAction) {
        val isUpperCase = mainInputMethodService.shiftState != MainInputMethodService.State.OFF
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
