package inc.flide.vim8.structures.yaml;

import android.view.KeyEvent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardActionType;

public class Action {
    @JsonProperty(value = "type")
    private KeyboardActionType actionType;
    private String lowerCase;
    private String upperCase;
    private List<FingerPosition> movementSequence;
    private int keyCode;

    private int flags;

    public Action() {
        actionType = KeyboardActionType.INPUT_TEXT;
        flags = 0;
        lowerCase = "";
        upperCase = "";
        movementSequence = new ArrayList<>();
        keyCode = 0;
    }

    public KeyboardActionType getActionType() {
        return actionType;
    }

    public void setActionType(KeyboardActionType actionType) {
        this.actionType = actionType;
    }

    public String getLowerCase() {
        return lowerCase;
    }

    public void setLowerCase(String lowerCase) {
        this.lowerCase = lowerCase;
    }

    public String getUpperCase() {
        return upperCase;
    }

    public void setUpperCase(String upperCase) {
        this.upperCase = upperCase;
    }

    public List<FingerPosition> getMovementSequence() {
        return movementSequence;
    }

    public void setMovementSequence(List<FingerPosition> movementSequence) {
        this.movementSequence = movementSequence;
    }

    public int getKeyCode() {
        return keyCode;
    }

    @JsonSetter("key_code")
    public void setKeyCode(String keyCodeString) {
        if (keyCodeString == null || keyCodeString.isEmpty()) {
            return;
        }
        keyCodeString = keyCodeString.toUpperCase(Locale.getDefault());
        //Strictly the inputKey has to has to be a Keycode from the KeyEvent class
        //Or it needs to be one of the customKeyCodes
        keyCode = KeyEvent.keyCodeFromString(keyCodeString);
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            try {
                keyCode = CustomKeycode.valueOf(keyCodeString).getKeyCode();
            } catch (IllegalArgumentException error) {
            }
        }
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isEmpty() {
        return (lowerCase == null || lowerCase.isEmpty())
            && (upperCase == null || upperCase.isEmpty())
            && movementSequence.isEmpty()
            && flags == 0;
    }
}
