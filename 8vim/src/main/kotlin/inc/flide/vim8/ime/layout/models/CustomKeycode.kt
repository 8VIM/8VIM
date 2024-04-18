package inc.flide.vim8.ime.layout.models

import android.view.KeyEvent

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
    CTRL_TOGGLE(-18),
    FN_TOGGLE(-19);

    fun toKeyEvent(): Int = when (this) {
        MOVE_CURRENT_END_POINT_LEFT, SELECTION_START -> KeyEvent.KEYCODE_DPAD_LEFT
        MOVE_CURRENT_END_POINT_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        MOVE_CURRENT_END_POINT_UP -> KeyEvent.KEYCODE_DPAD_UP
        MOVE_CURRENT_END_POINT_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        else -> 0
    }

    companion object {
        val KEY_CODE_TO_STRING_CODE_MAP: Map<Int, CustomKeycode> =
            entries.associateBy({ it.keyCode }, { it })
    }
}
