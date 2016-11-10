package inc.flide.eightvim.keyboardHelpers;

public enum InputSpecialKeyEventCode {

    SHIFT_TOOGLE("SHIFT_TOOGLE",0),
    SWITCH_TO_NUMBER_PAD("SWITCH_TO_NUMBER_PAD",1),
    PASTE("PASTE",2),
    SWITCH_TO_MAIN_KEYBOARD("SWITCH_TO_MAIN_KEYBOARD",3);

    private final String stringValue;
    private final int value;

    InputSpecialKeyEventCode(final String stringValue, final int value) {

        this.value = value;
        this.stringValue = stringValue;
    }

    public static InputSpecialKeyEventCode getInputSpecialKeyEventCodeWithName(final String newValue) {
        switch (newValue){
            case "SHIFT_TOOGLE":
                return SHIFT_TOOGLE;
            case "SWITCH_TO_NUMBER_PAD":
                return SWITCH_TO_NUMBER_PAD;
            case "SWITCH_TO_MAIN_KEYBOARD":
                return SWITCH_TO_MAIN_KEYBOARD;
            case "PASTE":
                return PASTE;
            default:
                return null;
        }
    }

    public String getName() {
        return stringValue;
    }
    public int getValue() { return value;}
}
