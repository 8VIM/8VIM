package inc.flide.vim8.structures;

import inc.flide.vim8.structures.yaml.Layout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private final Map<List<Integer>, KeyboardAction> actionMap = new HashMap<>();
    private final CharacterSet[] characterSets = new CharacterSet[Constants.MAX_LAYERS + 1];
    public int layoutPositions = 0;
    public int sectors = 1;
    private int totalLayers = 0;
    private Layout.LayoutInfo info;

    public Map<List<Integer>, KeyboardAction> getActionMap() {
        return actionMap;
    }

    public void addActionMap(List<Integer> movementSequence, KeyboardAction keyboardAction) {
        this.actionMap.put(movementSequence, keyboardAction);
    }

    public void addAllToActionMap(Map<List<Integer>, KeyboardAction> actionMapAddition) {
        this.actionMap.putAll(actionMapAddition);
    }

    public int getTotalLayers() {
        return totalLayers;
    }

    public String getLowerCaseCharacters(int layer) {
        if (layer < 0 || layer > totalLayers || characterSets[layer] == null) {
            return "";
        }
        return characterSets[layer].getLowerCaseCharacters();
    }


    public void setLowerCaseCharacters(String lowerCaseCharacters, int layer) {
        if (layer < 0 || layer > Constants.MAX_LAYERS) {
            return;
        }

        updateCharacterSets(layer);
        characterSets[layer].setLowerCaseCharacters(lowerCaseCharacters);
    }

    public String getUpperCaseCharacters(int layer) {
        if (layer < 0 || layer > totalLayers || characterSets[layer] == null) {
            return "";
        }
        return characterSets[layer].getUpperCaseCharacters();
    }


    public void setUpperCaseCharacters(String upperCaseCharacters, int layer) {
        if (layer < 0 || layer > Constants.MAX_LAYERS) {
            return;
        }

        updateCharacterSets(layer);
        characterSets[layer].setUpperCaseCharacters(upperCaseCharacters);
    }

    public int findLayer(List<Integer> movementSequence) {
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

    public Layout.LayoutInfo getInfo() {
        return info;
    }

    public void setInfo(Layout.LayoutInfo info) {
        this.info = info;
    }
}
