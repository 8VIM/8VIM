package inc.flide.vim8.keyboardActionListners

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.view.*
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.structures.KeyboardAction

open class KeypadActionListener {
    private val context: Context
    private val isSelectionOn = true
    private val audioManager: AudioManager
    protected var view: View

    constructor(v: View) {
        view = v
        context = view.context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun keyCodeIsValid(keyCode: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= KeyEvent.KEYCODE_PROFILE_SWITCH
        } else {
            keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= 288
        }
    }

    private fun customKeyCodeIsValid(keyCode: Int): Boolean {
        return keyCode <= KeyEvent.KEYCODE_UNKNOWN
    }

    fun handleInputKey(keyboardAction: KeyboardAction) {
        handleInputKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags())
    }

    fun handleInputKey(keyCode: Int, keyFlags: Int) {
        var actionHandled = handleKeyEventKeyCodes(keyCode, keyFlags)
        if (!actionHandled) {
            actionHandled = handleCustomKeyCodes(keyCode, keyFlags)
        }
        if (!actionHandled) {
            onText(keyCode.toString())
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
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.getInstance(context)
        val userEnabledHapticFeedback = pref.getBoolean(
                context.getString(R.string.pref_haptic_feedback_key),
                true)
        if (userEnabledHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        }
        val userEnabledSoundFeedback = pref
                .getBoolean(
                        context.getString(R.string.pref_sound_feedback_key),
                        true)
        if (userEnabledSoundFeedback) {
            audioManager.playSoundEffect(keySound)
        }
    }

    private fun handleCustomKeyCodes(customKeyEventCode: Int, keyFlags: Int): Boolean {
        when (CustomKeycode.fromIntValue(customKeyEventCode)) {
            CustomKeycode.MOVE_CURRENT_END_POINT_LEFT -> moveSelection(KeyEvent.KEYCODE_DPAD_LEFT)
            CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT -> moveSelection(KeyEvent.KEYCODE_DPAD_RIGHT)
            CustomKeycode.MOVE_CURRENT_END_POINT_UP -> moveSelection(KeyEvent.KEYCODE_DPAD_UP)
            CustomKeycode.MOVE_CURRENT_END_POINT_DOWN -> moveSelection(KeyEvent.KEYCODE_DPAD_DOWN)
            CustomKeycode.SELECTION_START -> {
                MainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                MainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                MainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
            }
            CustomKeycode.SELECT_ALL -> MainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
            CustomKeycode.TOGGLE_SELECTION_ANCHOR -> MainInputMethodService.switchAnchor()
            CustomKeycode.SHIFT_TOGGLE -> MainInputMethodService.performShiftToggle()
            CustomKeycode.SWITCH_TO_MAIN_KEYPAD -> MainInputMethodService.switchToMainKeypad()
            CustomKeycode.SWITCH_TO_NUMBER_KEYPAD -> MainInputMethodService.switchToNumberPad()
            CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD -> MainInputMethodService.switchToSymbolsKeypad()
            CustomKeycode.SWITCH_TO_SELECTION_KEYPAD -> MainInputMethodService.switchToSelectionKeypad()
            CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD -> MainInputMethodService.switchToExternalEmoticonKeyboard()
            CustomKeycode.HIDE_KEYBOARD -> MainInputMethodService.hideKeyboard()
            else -> return false
        }
        return true
    }

    private fun handleKeyEventKeyCodes(primaryCode: Int, keyFlags: Int): Boolean {
        if (keyCodeIsValid(primaryCode)) {
            when (primaryCode) {
                KeyEvent.KEYCODE_CUT -> MainInputMethodService.cut()
                KeyEvent.KEYCODE_COPY -> MainInputMethodService.copy()
                KeyEvent.KEYCODE_PASTE -> MainInputMethodService.paste()
                KeyEvent.KEYCODE_ENTER -> MainInputMethodService.commitImeOptionsBasedEnter()
                KeyEvent.KEYCODE_DEL -> MainInputMethodService.delete()
                else -> {
                    MainInputMethodService.sendKey(primaryCode, keyFlags)
                    MainInputMethodService.setShiftLockFlag(0)
                }
            }
            return true
        }
        return false
    }

    protected open fun onText(text: CharSequence?) {
        MainInputMethodService.sendText(text.toString())
        MainInputMethodService.setShiftLockFlag(0)
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD)
    }

    fun handleInputText(keyboardAction: KeyboardAction) {
        if (keyboardAction.getText()!!.length == 1
                && (MainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON ||
                    MainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON)) {
            onText(keyboardAction.getCapsLockText())
        } else {
            onText(keyboardAction.getText())
        }
    }

    private fun moveSelection(dpadKeyCode: Int) {
        if (isSelectionOn) {
            MainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }
        MainInputMethodService.sendDownAndUpKeyEvent(dpadKeyCode, 0)
        if (isSelectionOn) {
            MainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }
    }

}