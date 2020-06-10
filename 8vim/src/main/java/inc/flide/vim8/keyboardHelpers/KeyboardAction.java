package inc.flide.vim8.keyboardHelpers;

import inc.flide.vim8.structures.KeyboardActionType;

public class KeyboardAction {

    private KeyboardActionType keyboardActionType;
    private String text;
    private String capsLockText;
    private int keyEventCode;
    private int keyFlags;

    public KeyboardAction(KeyboardActionType keyboardActionType, String text, String capsLockText, int keyEventCode ,int keyFlags) {
        this.keyboardActionType = keyboardActionType;
        this.text = text;
        this.keyEventCode = keyEventCode;
        setCapsLockText(capsLockText);
        this.keyFlags = keyFlags;
    }

    private void setCapsLockText(String capsLockText) {
        if((capsLockText==null || capsLockText.length()==0) && this.text!=null){
            this.capsLockText = this.text.toUpperCase();
        } else{
            this.capsLockText = capsLockText;
        }
    }

    public int getKeyFlags() { return keyFlags; }

    public String getText() {
        return text;
    }

    public int getKeyEventCode() {
        return keyEventCode;
    }

    public KeyboardActionType getKeyboardActionType() {
        return keyboardActionType;
    }

    public String getCapsLockText() { return capsLockText; }
}
