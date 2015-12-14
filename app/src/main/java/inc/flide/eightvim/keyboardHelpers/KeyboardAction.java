package inc.flide.eightvim.keyboardHelpers;

/**
 * Created by flide on 23/11/15.
 */
public class KeyboardAction {
    public enum KeyboardActionType {INPUT_TEXT, INPUT_KEY, INPUT_SPECIAL};

    private KeyboardActionType keyboardActionType;
    private String text;
    private int keyEventCode;

    public KeyboardAction(KeyboardActionType keyboardActionType, String text, int keyEventCode) {
        this.keyboardActionType = keyboardActionType;
        this.text = text;
        this.keyEventCode = keyEventCode;
    }

    public String getText() {
        return text;
    }

    public int getKeyEventCode() {
        return keyEventCode;
    }

    public KeyboardActionType getKeyboardActionType() {
        return keyboardActionType;
    }
}
