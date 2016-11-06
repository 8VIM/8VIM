package inc.flide.eightvim.keyboardHelpers;

/**
 * Created by flide on 5/11/16.
 */

public enum InputSpecialKeyEventCode {

    SHIFT_TOOGLE(0),
    KEYBOARD_TOOGLE(1),
    PASTE(2);

    private final int value;

    InputSpecialKeyEventCode(final int newValue) {
        value = newValue;
    }

    public static InputSpecialKeyEventCode getInputSpecialKeyEventCodeWithValue(final int newValue) {
        switch (newValue){
            case 0:
                return SHIFT_TOOGLE;
            case 1:
                return KEYBOARD_TOOGLE;
            case 2:
                return PASTE;
            default:
                return null;
        }
    }

    public int getValue() {
        return value;
    }
}
