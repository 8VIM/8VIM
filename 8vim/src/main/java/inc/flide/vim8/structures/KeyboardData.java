package inc.flide.vim8.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private Map<List<FingerPosition>, KeyboardAction> actionMap;
    private final List<CharacterSet> characterSets;

    public KeyboardData() {
        actionMap = new HashMap<>();
        characterSets = new ArrayList<>();
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
        if (layer >= characterSets.size()) return "";
        return characterSets.get(layer).getLowerCaseCharacters();
    }


    public void setLowerCaseCharacters(String upperCaseCharacters, int layer) {
        updateCharacterSets(layer);
        characterSets.get(layer).setLowerCaseCharacters(upperCaseCharacters);
    }

    public String getUpperCaseCharacters(int layer) {
        if (layer >= characterSets.size()) return "";
        return characterSets.get(layer).getUpperCaseCharacters();
    }


    public void setUpperCaseCharacters(String upperCaseCharacters, int layer) {
        updateCharacterSets(layer);
        characterSets.get(layer).setUpperCaseCharacters(upperCaseCharacters);
    }

    public int totalLayers() {
        return characterSets.size();
    }

    public int findLayer(List<FingerPosition> movementSequence) {
        KeyboardAction action = actionMap.get(movementSequence);
        if (action == null) return Constants.DEFAULT_LAYER;
        return action.getLayer();
    }


    private void updateCharacterSets(int layer) {
        if (layer >= characterSets.size()) {
            for (int i = characterSets.size(); i <= layer; i++) {
                characterSets.add(new CharacterSet());
            }
        }

    }
}
