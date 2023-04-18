package inc.flide.vim8.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private Map<List<FingerPosition>, KeyboardAction> actionMap;
    private final CharacterSet[] characterSets;

    public KeyboardData() {
        actionMap = new HashMap<>();
        characterSets = new CharacterSet[Constants.MAX_LAYERS + 1];
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

    public String getLowerCaseCharacters(int layer) {
        if (layer > characterSets.length || characterSets[layer] == null) {
            return "";
        }
        return characterSets[layer].getLowerCaseCharacters();
    }


    public void setLowerCaseCharacters(String upperCaseCharacters, int layer) {
        updateCharacterSets(layer);
        characterSets[layer].setLowerCaseCharacters(upperCaseCharacters);
    }

    public String getUpperCaseCharacters(int layer) {
        if (layer > characterSets.length || characterSets[layer] == null) {
            return "";
        }
        return characterSets[layer].getUpperCaseCharacters();
    }


    public void setUpperCaseCharacters(String upperCaseCharacters, int layer) {
        updateCharacterSets(layer);
        characterSets[layer].setUpperCaseCharacters(upperCaseCharacters);
    }

    public int findLayer(List<FingerPosition> movementSequence) {
        KeyboardAction action = actionMap.get(movementSequence);
        if (action == null) {
            return Constants.DEFAULT_LAYER;
        }
        return action.getLayer();
    }


    private void updateCharacterSets(int layer) {
        if (characterSets[layer] == null) {
            characterSets[layer] = new CharacterSet();
        }
    }
}
