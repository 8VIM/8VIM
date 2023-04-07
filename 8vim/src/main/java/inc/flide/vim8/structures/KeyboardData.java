package inc.flide.vim8.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    private Map<List<FingerPosition>, KeyboardAction> actionMap;
    private final List<CharacterSet> characterSets;

    public TrieNode getActivationMovementSequences() {
        return activationMovementSequences;
    }

    private final TrieNode activationMovementSequences;

    public KeyboardData() {
        actionMap = new HashMap<>();
        characterSets = new ArrayList<>();
        activationMovementSequences = new TrieNode();
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
        int layer = activationMovementSequences.findLayer(movementSequence);
        return Math.max(0, layer);
    }


    public void addMovementSequence(List<FingerPosition> movementSequence, int layer) {
        if (movementSequence == null || layer <= 0) return;
        activationMovementSequences.addMovementSequence(movementSequence, layer);
    }

    public void addAllMovementSequence(TrieNode node) {
        activationMovementSequences.copy(node);
    }

    private void updateCharacterSets(int layer) {
        if (layer >= characterSets.size()) {
            for (int i = characterSets.size(); i <= layer; i++) {
                characterSets.add(new CharacterSet());
            }
        }

    }
}
