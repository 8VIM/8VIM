package inc.flide.vim8.structures;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CustomKeycode {
    MOVE_CURRENT_END_POINT_LEFT(-1),
    MOVE_CURRENT_END_POINT_RIGHT(-2),
    MOVE_CURRENT_END_POINT_UP(-3),
    MOVE_CURRENT_END_POINT_DOWN(-4),
    SELECTION_START(-5),
    SELECT_ALL(-6),
    TOGGLE_SELECTION_ANCHOR(-7),
    SHIFT_TOGGLE(-8),
    SWITCH_TO_MAIN_KEYPAD(-9),
    SWITCH_TO_NUMBER_KEYPAD(-10),
    SWITCH_TO_SYMBOLS_KEYPAD(-11),
    SWITCH_TO_SELECTION_KEYPAD(-12),
    SWITCH_TO_EMOTICON_KEYBOARD(-13),
    HIDE_KEYBOARD(-14),
    NO_OPERATION(-15);

    private static final Map<Integer, CustomKeycode> KEY_CODE_TO_STRING_CODE_MAP = new HashMap<>();

    static {
        for (CustomKeycode customKeycode : EnumSet.allOf(CustomKeycode.class)) {
            KEY_CODE_TO_STRING_CODE_MAP.put(customKeycode.getKeyCode(), customKeycode);
        }
    }

    private final int keyCode;

    CustomKeycode(int keyCode) {
        this.keyCode = keyCode;
    }

    public static CustomKeycode fromIntValue(int value) {
        return KEY_CODE_TO_STRING_CODE_MAP.get(value);
    }

    public int getKeyCode() {
        return keyCode;
    }
}
