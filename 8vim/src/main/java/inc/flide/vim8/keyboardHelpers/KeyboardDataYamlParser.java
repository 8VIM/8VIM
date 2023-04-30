package inc.flide.vim8.keyboardHelpers;

import android.view.KeyEvent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.yaml.Action;
import inc.flide.vim8.structures.yaml.Direction;
import inc.flide.vim8.structures.yaml.ExtraLayer;
import inc.flide.vim8.structures.yaml.Layer;
import inc.flide.vim8.structures.yaml.Layout;
import inc.flide.vim8.structures.yaml.Part;
import inc.flide.vim8.utils.QuadrantHelper;

public class KeyboardDataYamlParser {
    private final Layout layout;

    public KeyboardDataYamlParser(InputStream inputStream) throws IOException {
        ObjectMapper mapper = YAMLMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .build();
        layout = mapper.readValue(inputStream, Layout.class);
    }

    public KeyboardData readKeyboardData() {
        KeyboardData keyboardData = new KeyboardData();
        if (!layout.getHidden().isEmpty()) {
            addKeyboardActions(keyboardData, layout.getHidden());
        }

        Layer defaultLayer = layout.getDefaultLayer();
        Map<ExtraLayer, Layer> extraLayers = layout.getExtraLayers();

        if (!extraLayers.isEmpty() && defaultLayer == null) {
            return keyboardData;
        }

        if (defaultLayer != null) {
            addLayer(keyboardData, Constants.DEFAULT_LAYER, defaultLayer);
        }

        for (Map.Entry<ExtraLayer, Layer> extraLayerMapEntry : extraLayers.entrySet()) {
            int layer = extraLayerMapEntry.getKey().ordinal() + 2;
            addLayer(keyboardData, layer, extraLayerMapEntry.getValue());
        }

        return keyboardData;
    }

    private void addLayer(KeyboardData keyboardData, int layer, Layer layerData) {
        StringBuilder lowerCaseCharacters = new StringBuilder();
        StringBuilder upperCaseCharacters = new StringBuilder();

        for (Map.Entry<Direction, Part> sectorEntry : layerData.getSectors().entrySet()) {
            Direction sector = sectorEntry.getKey();

            for (Map.Entry<Direction, List<Action>> partEntry : sectorEntry.getValue().getParts().entrySet()) {
                Direction part = partEntry.getKey();
                Quadrant quadrant = QuadrantHelper.getQuadrant(sector, part);

                if (quadrant == null) {
                    continue;
                }

                addKeyboardActions(keyboardData, layer, quadrant, partEntry.getValue(), lowerCaseCharacters, upperCaseCharacters);
            }
        }

        keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
        keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
    }

    private void addKeyboardActions(KeyboardData keyboardData, List<Action> actions) {
        for (Action action : actions) {
            List<FingerPosition> movementSequence = action.getMovementSequence();

            if (movementSequence == null || movementSequence.isEmpty()) {
                continue;
            }

            int keyCode = getKeyCode(action.getKeyCode());
            KeyboardAction actionMap =
                new KeyboardAction(action.getActionType(), action.getLowerCase(), action.getUpperCase(), keyCode, action.getFlags(),
                    Constants.HIDDEN_LAYER);
            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }

    private void addKeyboardActions(KeyboardData keyboardData, int layer, Quadrant quadrant, List<Action> actions,
                                    StringBuilder lowerCaseCharacters,
                                    StringBuilder upperCaseCharacters) {
        int actionsSize = Math.min(actions.size(), 4);
        for (int position = 0; position < actionsSize; position++) {
            Action action = actions.get(position);
            if (action == null || action.isEmpty()) {
                continue;
            }

            List<FingerPosition> movementSequence = action.getMovementSequence();

            if (movementSequence == null || movementSequence.isEmpty()) {
                movementSequence = QuadrantHelper.computeMovementSequence(layer, quadrant, position);
            }

            int characterSetIndex = getCharacterSetIndex(quadrant, position);
            if (action.getLowerCase() != null && !action.getLowerCase().isEmpty()) {
                if (lowerCaseCharacters.length() == 0) {
                    lowerCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                }
                lowerCaseCharacters.setCharAt(characterSetIndex, action.getLowerCase().charAt(0));

                if (action.getUpperCase() == null || action.getUpperCase().isEmpty()) {
                    action.setUpperCase(action.getLowerCase().toUpperCase(Locale.ROOT));
                }
            }

            if (action.getUpperCase() != null && !action.getUpperCase().isEmpty()) {
                if (upperCaseCharacters.length() == 0) {
                    upperCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                }
                upperCaseCharacters.setCharAt(characterSetIndex, action.getUpperCase().charAt(0));
            }

            int keyCode = getKeyCode(action.getKeyCode());
            inc.flide.vim8.structures.KeyboardAction actionMap =
                new inc.flide.vim8.structures.KeyboardAction(action.getActionType(), action.getLowerCase(), action.getUpperCase(), keyCode,
                    action.getFlags(), layer);
            keyboardData.addActionMap(movementSequence, actionMap);
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
