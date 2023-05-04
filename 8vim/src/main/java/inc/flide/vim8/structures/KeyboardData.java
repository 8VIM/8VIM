package inc.flide.vim8.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private final Map<List<FingerPosition>, KeyboardAction> actionMap = new HashMap<>();
    private final CharacterSet[] characterSets = new CharacterSet[Constants.MAX_LAYERS + 1];
    private int totalLayers = 0;

    public Map<List<FingerPosition>, KeyboardAction> getActionMap() {
        return actionMap;
    }

    public void addActionMap(List<FingerPosition> movementSequence, KeyboardAction keyboardAction) {
        this.actionMap.put(movementSequence, keyboardAction);
    }

    public void addAllToActionMap(Map<List<FingerPosition>, KeyboardAction> actionMapAddition) {
        this.actionMap.putAll(actionMapAddition);
    }

    public int getTotalLayer() {
        return totalLayers;
    }

    public String getLowerCaseCharacters(int layer) {
        if (layer > totalLayers || characterSets[layer] == null) {
            return "";
        }
        return characterSets[layer].getLowerCaseCharacters();
    }


    public void setLowerCaseCharacters(String upperCaseCharacters, int layer) {
        updateCharacterSets(layer);
        characterSets[layer].setLowerCaseCharacters(upperCaseCharacters);
    }

    public String getUpperCaseCharacters(int layer) {
        if (layer > totalLayers || characterSets[layer] == null) {
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
        totalLayers = Math.max(totalLayers, layer);
        if (characterSets[layer] == null) {
            characterSets[layer] = new CharacterSet();
        }
    }
}
