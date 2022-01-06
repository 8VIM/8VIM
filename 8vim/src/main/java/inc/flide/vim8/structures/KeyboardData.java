package inc.flide.vim8.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private Map<List<FingerPosition>, KeyboardAction> actionMap;
    private String lowerCaseCharacters;
    private String upperCaseCharacters;

    public KeyboardData() {
        actionMap = new HashMap<>();
        lowerCaseCharacters = "";
        upperCaseCharacters = "";
    }

    public Map<List<FingerPosition>, KeyboardAction> getActionMap() {
        return actionMap;
    }

    public void setActionMap(Map<List<FingerPosition>, KeyboardAction> actionMap) {
        this.actionMap = actionMap;
    }

    public void addAllToActionMap(Map<List<FingerPosition>, KeyboardAction> actionMapAddition) {
        this.actionMap.putAll(actionMapAddition);
    }

    public String getLowerCaseCharacters() {
        return lowerCaseCharacters;
    }

    public void setLowerCaseCharacters(String lowerCaseCharacters) {
        this.lowerCaseCharacters = lowerCaseCharacters;
    }

    public String getUpperCaseCharacters() {
        return upperCaseCharacters;
    }

    public void setUpperCaseCharacters(String upperCaseCharacters) {
        this.upperCaseCharacters = upperCaseCharacters;
    }
}
