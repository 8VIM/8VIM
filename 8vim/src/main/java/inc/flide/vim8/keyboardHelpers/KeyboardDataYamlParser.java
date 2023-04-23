package inc.flide.vim8.keyboardHelpers;

import android.view.KeyEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.yaml.KeyboardAction;
import inc.flide.vim8.structures.yaml.Layers;
import inc.flide.vim8.utils.QuadrantHelper;

public class KeyboardDataYamlParser {
    private Layers layers;

    public KeyboardDataYamlParser(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        layers = mapper.readValue(inputStream, Layers.class);
    }

    public KeyboardData readKeyboardData() {
        KeyboardData keyboardData = new KeyboardData();
        if (layers.getHidden() != null && !layers.getHidden().isEmpty()) {
            addSectors(keyboardData, 0, layers.getHidden());
        }
        if (layers.getVisible() != null && !layers.getVisible().isEmpty()) {
            List<Map<Quadrant, List<KeyboardAction>>> visible = layers.getVisible();
            for (int i = 0; i < visible.size(); i++) {
                Map<Quadrant, List<KeyboardAction>> layer = visible.get(i);
                addSectors(keyboardData, i + 1, layer);
            }
        }
        return keyboardData;
    }

    private void addSectors(KeyboardData keyboardData, int layer, Map<Quadrant, List<KeyboardAction>> sectors) {
        StringBuilder lowerCaseCharacters = new StringBuilder();
        StringBuilder upperCaseCharacters = new StringBuilder();
        for (Map.Entry<Quadrant, List<KeyboardAction>> entry : sectors.entrySet()) {
            Quadrant quadrant = entry.getKey();
            List<KeyboardAction> actions = entry.getValue();
            for (int position = 0; position < actions.size(); position++) {
                boolean isVisible = layer >= Constants.DEFAULT_LAYER && quadrant != Quadrant.NO_SECTOR;

                if (isVisible && position >= 4) {
                    continue;
                }


                KeyboardAction action = actions.get(position);
                if (action == null || action.isEmpty()) {
                    continue;
                }

                List<FingerPosition> movementSequence = action.getMovementSequence();

                if (movementSequence == null || movementSequence.isEmpty()) {
                    movementSequence = QuadrantHelper.computeMovementSequence(layer, quadrant, position);
                }

                if (isVisible) {
                    int characterSetIndex = getCharacterSetIndex(quadrant, position);
                    if (action.getLowerCase() != null && !action.getLowerCase().isEmpty()) {
                        if (lowerCaseCharacters.length() == 0) {
                            lowerCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                        }
                        lowerCaseCharacters.setCharAt(characterSetIndex, action.getLowerCase().charAt(0));
                    }

                    if (action.getUpperCase() != null && !action.getUpperCase().isEmpty()) {
                        if (upperCaseCharacters.length() == 0) {
                            upperCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                        }
                        upperCaseCharacters.setCharAt(characterSetIndex, action.getUpperCase().charAt(0));
                    }
                }

                int keyCode = getKeyCode(action.getKeyCode());
                inc.flide.vim8.structures.KeyboardAction actionMap =
                    new inc.flide.vim8.structures.KeyboardAction(action.getActionType(), action.getLowerCase(), action.getUpperCase(), keyCode,
                        action.getFlags(), layer);
                keyboardData.addActionMap(movementSequence, actionMap);
            }
        }
        if (layer >= Constants.DEFAULT_LAYER) {
            keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
            keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
        }
    }

    private int getKeyCode(String keyCodeString) {
        int keyCode = 0;
        if (keyCodeString == null || keyCodeString.isEmpty()) {
            return keyCode;
        }

        //Strictly the inputKey has to has to be a Keycode from the KeyEvent class
        //Or it needs to be one of the customKeyCodes
        keyCode = KeyEvent.keyCodeFromString(keyCodeString);
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            try {
                keyCode = CustomKeycode.valueOf(keyCodeString).getKeyCode();
            } catch (IllegalArgumentException error) {
                keyCode = KeyEvent.KEYCODE_UNKNOWN;
            }
        }

        return keyCode;
    }

    private int getCharacterSetIndex(Quadrant quadrant, int position) {
        int base = quadrant.ordinal() / 2 * 8;
        int delta = quadrant.ordinal() % 2;
        return base + position * 2 + delta;
    }
}
