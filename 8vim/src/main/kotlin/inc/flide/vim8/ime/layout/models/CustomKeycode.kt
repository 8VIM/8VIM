package inc.flide.vim8.ime.layout.models

import android.view.KeyEvent
import inc.flide.vim8.Vim8ImeService

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

    fun toKeyEvent(): Int = when (this) {
        MOVE_CURRENT_END_POINT_LEFT, SELECTION_START -> KeyEvent.KEYCODE_DPAD_LEFT
        MOVE_CURRENT_END_POINT_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        MOVE_CURRENT_END_POINT_UP -> KeyEvent.KEYCODE_DPAD_UP
        MOVE_CURRENT_END_POINT_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        else -> 0
    }

    fun handleKeyCode(vim8ImeService: Vim8ImeService): Boolean {
        when (this) {
            MOVE_CURRENT_END_POINT_LEFT,
            MOVE_CURRENT_END_POINT_RIGHT,
            MOVE_CURRENT_END_POINT_UP,
            MOVE_CURRENT_END_POINT_DOWN -> {
//                if (vim8ImeService.shiftState != Vim8ImeService.State.OFF) {
//                    vim8ImeService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
//                }
//                vim8ImeService.sendDownAndUpKeyEvent(
//                    dPadKeyCodeFromCustom,
//                    vim8ImeService.ctrlFlag
//                )
//                if (vim8ImeService.shiftState != Vim8ImeService.State.OFF) {
//                    vim8ImeService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
//                }
            }

            SELECTION_START -> {
                /*      vim8ImeService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
                      vim8ImeService.sendDownAndUpKeyEvent(
                          KeyEvent.KEYCODE_DPAD_LEFT,
                          vim8ImeService.ctrlFlag
                      )
                      vim8ImeService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)*/
            }

            SELECT_ALL -> vim8ImeService.sendDownAndUpKeyEvent(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON
            )

            TOGGLE_SELECTION_ANCHOR -> vim8ImeService.switchAnchor()
            SHIFT_TOGGLE -> vim8ImeService.performShiftToggle()
            SWITCH_TO_MAIN_KEYPAD -> vim8ImeService.switchToMainKeypad()
            SWITCH_TO_NUMBER_KEYPAD -> vim8ImeService.switchToNumberPad()
            SWITCH_TO_SYMBOLS_KEYPAD -> vim8ImeService.switchToSymbolsKeypad()
            SWITCH_TO_SELECTION_KEYPAD -> vim8ImeService.switchToSelectionKeypad()
            SWITCH_TO_EMOTICON_KEYBOARD -> vim8ImeService.switchToExternalEmoticonKeyboard()
            HIDE_KEYBOARD -> vim8ImeService.hideKeyboard()
            SWITCH_TO_CLIPPAD_KEYBOARD -> vim8ImeService.switchToClipboardKeypad()
            CTRL_TOGGLE -> vim8ImeService.performCtrlToggle()
            else -> {}
        }
        return true
    }

    companion object {
        val KEY_CODE_TO_STRING_CODE_MAP: Map<Int, CustomKeycode> =
            CustomKeycode.values().associateBy({ it.keyCode }, { it })
    }
}
