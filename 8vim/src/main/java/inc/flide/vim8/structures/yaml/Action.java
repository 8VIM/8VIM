package inc.flide.vim8.structures.yaml;

import java.util.List;

import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardActionType;

public class Action {
    private KeyboardActionType actionType;
    private String lowerCase;
    private String upperCase;
    private List<FingerPosition> movementSequence;
    private String keyCode;
    private int flags;

    public Action() {
        flags = 0;
        actionType = KeyboardActionType.INPUT_TEXT;
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

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
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
            && (movementSequence == null || movementSequence.isEmpty())
            && (keyCode == null || keyCode.isEmpty())
            && actionType == null && flags == 0;
    }
}
