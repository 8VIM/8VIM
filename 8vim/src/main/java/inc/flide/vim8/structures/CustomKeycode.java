package inc.flide.vim8.structures;

import android.view.KeyEvent;
import inc.flide.vim8.MainInputMethodService;
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
    NO_OPERATION(-15),
    SWITCH_TO_CLIPPAD_KEYBOARD(-16);

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

    public boolean handleKeyCode(MainInputMethodService mainInputMethodService) {
        switch (this) {
            case MOVE_CURRENT_END_POINT_LEFT:
            case MOVE_CURRENT_END_POINT_RIGHT:
            case MOVE_CURRENT_END_POINT_UP:
            case MOVE_CURRENT_END_POINT_DOWN:
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                mainInputMethodService.sendDownAndUpKeyEvent(getDPadKeyCodeFromCustom(), 0);
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                break;
            case SELECTION_START:
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0);
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                break;
            case SELECT_ALL:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                break;
            case TOGGLE_SELECTION_ANCHOR:
                mainInputMethodService.switchAnchor();
                break;
            case SHIFT_TOGGLE:
                mainInputMethodService.performShiftToggle();
                break;
            case SWITCH_TO_MAIN_KEYPAD:
                mainInputMethodService.switchToMainKeypad();
                break;
            case SWITCH_TO_NUMBER_KEYPAD:
                mainInputMethodService.switchToNumberPad();
                break;
            case SWITCH_TO_SYMBOLS_KEYPAD:
                mainInputMethodService.switchToSymbolsKeypad();
                break;
            case SWITCH_TO_SELECTION_KEYPAD:
                mainInputMethodService.switchToSelectionKeypad();
                break;
            case SWITCH_TO_EMOTICON_KEYBOARD:
                mainInputMethodService.switchToExternalEmoticonKeyboard();
                break;
            case HIDE_KEYBOARD:
                mainInputMethodService.hideKeyboard();
                break;
            case NO_OPERATION:
                break;
            case SWITCH_TO_CLIPPAD_KEYBOARD:
                mainInputMethodService.switchToClipboardKeypad();
            default:
                return false;
        }
        return true;
    }

    private int getDPadKeyCodeFromCustom() {
        switch (this) {
            case MOVE_CURRENT_END_POINT_LEFT:
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case MOVE_CURRENT_END_POINT_RIGHT:
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            case MOVE_CURRENT_END_POINT_UP:
                return KeyEvent.KEYCODE_DPAD_UP;
            case MOVE_CURRENT_END_POINT_DOWN:
                return KeyEvent.KEYCODE_DPAD_DOWN;
            default:
                return 0;
        }
    }
}
