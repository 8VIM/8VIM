package inc.flide.vim8.ime.layout.models

import android.view.KeyEvent
import inc.flide.vim8.MainInputMethodService

enum class CustomKeycode(@JvmField val keyCode: Int) {
    MOVE_CURRENT_END_POINT_LEFT(-2),
    MOVE_CURRENT_END_POINT_RIGHT(-3),
    MOVE_CURRENT_END_POINT_UP(-4),
    MOVE_CURRENT_END_POINT_DOWN(-5),
    SELECTION_START(-6),
    SELECT_ALL(-7),
    TOGGLE_SELECTION_ANCHOR(-8),
    SHIFT_TOGGLE(-9),
    SWITCH_TO_MAIN_KEYPAD(-10),
    SWITCH_TO_NUMBER_KEYPAD(-11),
    SWITCH_TO_SYMBOLS_KEYPAD(-12),
    SWITCH_TO_SELECTION_KEYPAD(-13),
    SWITCH_TO_EMOTICON_KEYBOARD(-14),
    HIDE_KEYBOARD(-15),
    NO_OPERATION(-16),
    SWITCH_TO_CLIPPAD_KEYBOARD(-17),
    CTRL_TOGGLE(-18);

    fun handleKeyCode(mainInputMethodService: MainInputMethodService): Boolean {
        when (this) {
            MOVE_CURRENT_END_POINT_LEFT,
            MOVE_CURRENT_END_POINT_RIGHT,
            MOVE_CURRENT_END_POINT_UP,
            MOVE_CURRENT_END_POINT_DOWN -> {
                if (mainInputMethodService.shiftState != MainInputMethodService.State.OFF) {
                    mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                }
                mainInputMethodService.sendDownAndUpKeyEvent(
                    dPadKeyCodeFromCustom,
                    mainInputMethodService.ctrlFlag
                )
                if (mainInputMethodService.shiftState != MainInputMethodService.State.OFF) {
                    mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                }
            }

            SELECTION_START -> {
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                mainInputMethodService.sendDownAndUpKeyEvent(
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    mainInputMethodService.ctrlFlag
                )
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
            }

            SELECT_ALL -> mainInputMethodService.sendDownAndUpKeyEvent(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON
            )

            TOGGLE_SELECTION_ANCHOR -> mainInputMethodService.switchAnchor()
            SHIFT_TOGGLE -> mainInputMethodService.performShiftToggle()
            SWITCH_TO_MAIN_KEYPAD -> mainInputMethodService.switchToMainKeypad()
            SWITCH_TO_NUMBER_KEYPAD -> mainInputMethodService.switchToNumberPad()
            SWITCH_TO_SYMBOLS_KEYPAD -> mainInputMethodService.switchToSymbolsKeypad()
            SWITCH_TO_SELECTION_KEYPAD -> mainInputMethodService.switchToSelectionKeypad()
            SWITCH_TO_EMOTICON_KEYBOARD -> mainInputMethodService.switchToExternalEmoticonKeyboard()
            HIDE_KEYBOARD -> mainInputMethodService.hideKeyboard()
            SWITCH_TO_CLIPPAD_KEYBOARD -> mainInputMethodService.switchToClipboardKeypad()
            CTRL_TOGGLE -> mainInputMethodService.performCtrlToggle()
            else -> {}
        }
        return true
    }

    private val dPadKeyCodeFromCustom: Int
        get() = when (this) {
            MOVE_CURRENT_END_POINT_LEFT, SELECTION_START -> KeyEvent.KEYCODE_DPAD_LEFT
            MOVE_CURRENT_END_POINT_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            MOVE_CURRENT_END_POINT_UP -> KeyEvent.KEYCODE_DPAD_UP
            MOVE_CURRENT_END_POINT_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
            else -> 0
        }

    companion object {
        val KEY_CODE_TO_STRING_CODE_MAP: Map<Int, CustomKeycode> =
            CustomKeycode.values().associateBy({ it.keyCode }, { it })
    }
}
