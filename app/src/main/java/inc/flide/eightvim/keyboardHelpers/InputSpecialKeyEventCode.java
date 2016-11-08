package inc.flide.eightvim.keyboardHelpers;

public enum InputSpecialKeyEventCode {

    SHIFT_TOOGLE("SHIFT_TOOGLE",0),
    KEYBOARD_TOOGLE("KEYBOARD_TOOGLE",1),
    PASTE("PASTE",2);

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
            case "KEYBOARD_TOOGLE":
                return KEYBOARD_TOOGLE;
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
