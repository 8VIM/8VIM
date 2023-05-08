package inc.flide.vim8.structures;

import java.util.Objects;

public class KeyboardAction {
    private final KeyboardActionType keyboardActionType;
    private final String text;
    private final String capsLockText;
    private final int keyEventCode;
    private final int keyFlags;
    private final int layer;

    public KeyboardAction(KeyboardActionType keyboardActionType, String text, String capsLockText, int keyEventCode, int keyFlags, int layer) {
        this.keyboardActionType = keyboardActionType;
        this.text = text;
        this.keyEventCode = keyEventCode;
        this.layer = layer;
        this.capsLockText = capsLockText;
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
        if (capsLockText == null) {
            return "";
        }

        return capsLockText;
    }

    public int getLayer() {
        return layer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyboardAction that = (KeyboardAction) o;
        return keyEventCode == that.keyEventCode
            && keyFlags == that.keyFlags
            && layer == that.layer
            && keyboardActionType == that.keyboardActionType
            && Objects.equals(text, that.text)
            && Objects.equals(capsLockText, that.capsLockText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyboardActionType, text, capsLockText, keyEventCode, keyFlags, layer);
    }
}
