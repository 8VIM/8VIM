package inc.flide.vim8.structures;

import java.util.Locale;

public class KeyboardAction {

    private final KeyboardActionType keyboardActionType;
    private final String text;
    private String capsLockText;
    private final int keyEventCode;
    private final int keyFlags;
    private final int layer;

    public KeyboardAction(KeyboardActionType keyboardActionType, String text, String capsLockText, int keyEventCode, int keyFlags, int layer) {
        this.keyboardActionType = keyboardActionType;
        this.text = text;
        this.keyEventCode = keyEventCode;
        this.layer = layer;
        setCapsLockText(capsLockText);
        this.keyFlags = keyFlags;
    }

    public int getKeyFlags() {
        return keyFlags;
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

    public String getCapsLockText() {
        if(capsLockText == null) {
            return "";
        }
        
        return capsLockText;
    }

    private void setCapsLockText(String capsLockText) {
        if ((capsLockText == null || capsLockText.length() == 0) && this.text != null) {
            this.capsLockText = this.text.toUpperCase(Locale.getDefault());
        } else {
            this.capsLockText = capsLockText;
        }
    }

    public int getLayer() {
        return layer;
    }
}
