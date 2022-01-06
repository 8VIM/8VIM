package inc.flide.vim8.structures

import java.util.*

enum class CustomKeycode(private val keyCode: Int) {
    MOVE_CURRENT_END_POINT_LEFT(-1), MOVE_CURRENT_END_POINT_RIGHT(-2), MOVE_CURRENT_END_POINT_UP(-3), MOVE_CURRENT_END_POINT_DOWN(-4), SELECTION_START(-5), SELECT_ALL(-6), TOGGLE_SELECTION_ANCHOR(-7), SHIFT_TOGGLE(-8), SWITCH_TO_MAIN_KEYPAD(-9), SWITCH_TO_NUMBER_KEYPAD(-10), SWITCH_TO_SYMBOLS_KEYPAD(-11), SWITCH_TO_SELECTION_KEYPAD(-12), SWITCH_TO_EMOTICON_KEYBOARD(-13), HIDE_KEYBOARD(-14);

    companion object {
        private val KEY_CODE_TO_STRING_CODE_MAP: MutableMap<Int?, CustomKeycode?>? = null
        fun fromIntValue(value: Int): CustomKeycode? {
            return KEY_CODE_TO_STRING_CODE_MAP.get(value)
        }

        init {
            KEY_CODE_TO_STRING_CODE_MAP = HashMap()
            for (customKeycode in EnumSet.allOf(CustomKeycode::class.java)) {
                KEY_CODE_TO_STRING_CODE_MAP[inc.flide.vim8.structures.customKeycode.getKeyCode()] = inc.flide.vim8.structures.customKeycode
            }
        }
    }

    fun getKeyCode(): Int {
        return keyCode
    }
}