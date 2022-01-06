package inc.flide.vim8.keyboardActionListners

import android.content.Context
import android.media.AudioManager
import android.view.*
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.structures.KeyboardAction

open class KeypadActionListener(protected var mainInputMethodService: MainInputMethodService?, protected var view: View?) {
    private val isSelectionOn = true
    private val audioManager: AudioManager?
    private fun keyCodeIsValid(keyCode: Int): Boolean {
        return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= KeyEvent.KEYCODE_PROFILE_SWITCH
    }

    private fun customKeyCodeIsValid(keyCode: Int): Boolean {
        return keyCode <= KeyEvent.KEYCODE_UNKNOWN
    }

    fun handleInputKey(keyboardAction: KeyboardAction?) {
        handleInputKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags())
    }

    fun handleInputKey(keyCode: Int, keyFlags: Int) {
        var actionHandled = handleKeyEventKeyCodes(keyCode, keyFlags)
        if (!actionHandled) {
            actionHandled = handleCustomKeyCodes(keyCode, keyFlags)
        }
        if (!actionHandled) {
            onText(keyCode as Char.toString())
        }
        if (actionHandled) {
            performInputAcceptedFeedback(keySound(keyCode))
        }
    }

    private fun keySound(keyCode: Int): Int {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> return AudioManager.FX_KEYPRESS_RETURN
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> return AudioManager.FX_KEYPRESS_DELETE
            KeyEvent.KEYCODE_SPACE -> return AudioManager.FX_KEYPRESS_SPACEBAR
        }
        return AudioManager.FX_KEYPRESS_STANDARD
    }

    private fun performInputAcceptedFeedback(keySound: Int) {
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.Companion.getInstance(mainInputMethodService)
        val userEnabledHapticFeedback = pref.getBoolean(
                mainInputMethodService.getString(R.string.pref_haptic_feedback_key),
                true)
        if (userEnabledHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        }
        val userEnabledSoundFeedback = pref
                .getBoolean(
                        mainInputMethodService.getString(R.string.pref_sound_feedback_key),
                        true)
        if (userEnabledSoundFeedback) {
            audioManager.playSoundEffect(keySound)
        }
    }

    private fun handleCustomKeyCodes(customKeyEventCode: Int, keyFlags: Int): Boolean {
        when (CustomKeycode.Companion.fromIntValue(customKeyEventCode)) {
            CustomKeycode.MOVE_CURRENT_END_POINT_LEFT -> moveSelection(KeyEvent.KEYCODE_DPAD_LEFT)
            CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT -> moveSelection(KeyEvent.KEYCODE_DPAD_RIGHT)
            CustomKeycode.MOVE_CURRENT_END_POINT_UP -> moveSelection(KeyEvent.KEYCODE_DPAD_UP)
            CustomKeycode.MOVE_CURRENT_END_POINT_DOWN -> moveSelection(KeyEvent.KEYCODE_DPAD_DOWN)
            CustomKeycode.SELECTION_START -> {
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
            }
            CustomKeycode.SELECT_ALL -> mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
            CustomKeycode.TOGGLE_SELECTION_ANCHOR -> mainInputMethodService.switchAnchor()
            CustomKeycode.SHIFT_TOGGLE -> mainInputMethodService.performShiftToggle()
            CustomKeycode.SWITCH_TO_MAIN_KEYPAD -> mainInputMethodService.switchToMainKeypad()
            CustomKeycode.SWITCH_TO_NUMBER_KEYPAD -> mainInputMethodService.switchToNumberPad()
            CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD -> mainInputMethodService.switchToSymbolsKeypad()
            CustomKeycode.SWITCH_TO_SELECTION_KEYPAD -> mainInputMethodService.switchToSelectionKeypad()
            CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD -> mainInputMethodService.switchToExternalEmoticonKeyboard()
            CustomKeycode.HIDE_KEYBOARD -> mainInputMethodService.hideKeyboard()
            else -> return false
        }
        return true
    }

    fun handleKeyEventKeyCodes(primaryCode: Int, keyFlags: Int): Boolean {
        if (keyCodeIsValid(primaryCode)) {
            when (primaryCode) {
                KeyEvent.KEYCODE_CUT -> mainInputMethodService.cut()
                KeyEvent.KEYCODE_COPY -> mainInputMethodService.copy()
                KeyEvent.KEYCODE_PASTE -> mainInputMethodService.paste()
                KeyEvent.KEYCODE_ENTER -> mainInputMethodService.commitImeOptionsBasedEnter()
                KeyEvent.KEYCODE_DEL -> mainInputMethodService.delete()
                else -> {
                    mainInputMethodService.sendKey(primaryCode, keyFlags)
                    mainInputMethodService.setShiftLockFlag(0)
                }
            }
            return true
        }
        return false
    }

    fun onText(text: CharSequence?) {
        mainInputMethodService.sendText(text.toString())
        mainInputMethodService.setShiftLockFlag(0)
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD)
    }

    fun handleInputText(keyboardAction: KeyboardAction?) {
        if (keyboardAction.getText().length == 1
                && (isShiftSet() || isCapsLockSet())) {
            onText(keyboardAction.getCapsLockText())
        } else {
            onText(keyboardAction.getText())
        }
    }

    private fun moveSelection(dpadKeyCode: Int) {
        if (isSelectionOn) {
            mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }
        mainInputMethodService.sendDownAndUpKeyEvent(dpadKeyCode, 0)
        if (isSelectionOn) {
            mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }
    }

    fun areCharactersCapitalized(): Boolean {
        return mainInputMethodService.areCharactersCapitalized()
    }

    fun setModifierFlags(modifierFlags: Int) {
        mainInputMethodService.setModifierFlags(modifierFlags)
    }

    fun isShiftSet(): Boolean {
        return mainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON
    }

    fun isCapsLockSet(): Boolean {
        return mainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON
    }

    init {
        audioManager = view.getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}