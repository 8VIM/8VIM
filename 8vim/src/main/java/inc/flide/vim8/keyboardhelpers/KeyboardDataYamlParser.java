package inc.flide.vim8.keyboardhelpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.SectorPart;
import inc.flide.vim8.structures.yaml.Action;
import inc.flide.vim8.structures.yaml.ExtraLayer;
import inc.flide.vim8.structures.yaml.Layer;
import inc.flide.vim8.structures.yaml.Layout;
import inc.flide.vim8.structures.yaml.Part;
import inc.flide.vim8.utils.MovementSequenceHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class KeyboardDataYamlParser {
    private final ObjectMapper mapper;
    private final InputStream inputStream;

    public KeyboardDataYamlParser(InputStream inputStream) {
        mapper =
                YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
        this.inputStream = inputStream;
    }

    public static int isValidFile(InputStream inputStream) throws IOException {
        KeyboardDataYamlParser parser = new KeyboardDataYamlParser(inputStream);
        KeyboardData keyboardData = parser.readKeyboardData();
        return keyboardData.getTotalLayers();
    }

    public KeyboardData readKeyboardData() throws IOException {
        Layout layout = mapper.readValue(inputStream, Layout.class);
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
        Pair<StringBuilder, StringBuilder> characterSets = Pair.of(lowerCaseCharacters, upperCaseCharacters);

        for (Map.Entry<SectorPart, Part> sectorEntry : layerData.getSectors().entrySet()) {
            SectorPart sector = sectorEntry.getKey();

            for (Map.Entry<SectorPart, List<Action>> partEntry : sectorEntry.getValue().getParts().entrySet()) {
                SectorPart part = partEntry.getKey();
                Pair<SectorPart, SectorPart> sectorParts = Pair.of(sector, part);
                addKeyboardActions(keyboardData, layer, sectorParts, partEntry.getValue(), characterSets);
            }
        }

        keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
        keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
    }

    private void addKeyboardActions(KeyboardData keyboardData, List<Action> actions) {
        for (Action action : actions) {
            List<FingerPosition> movementSequence = action.getMovementSequence();

            if (movementSequence.isEmpty()) {
                continue;
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.getActionType(), action.getLowerCase(), action.getUpperCase(),
                            action.getKeyCode(), action.getFlags(),
                            Constants.HIDDEN_LAYER);
            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }

    private void addKeyboardActions(KeyboardData keyboardData, int layer, Pair<SectorPart, SectorPart> sectorParts,
                                    List<Action> actions,
                                    Pair<StringBuilder, StringBuilder> characterSets) {
        int actionsSize = Math.min(actions.size(), 4);

        for (int i = 0; i < actionsSize; i++) {
            Action action = actions.get(i);
            if (action == null || action.isEmpty()) {
                continue;
            }

            CharacterPosition characterPosition = CharacterPosition.values()[i];

            List<FingerPosition> movementSequence = action.getMovementSequence();

            if (movementSequence.isEmpty()) {
                movementSequence =
                        MovementSequenceHelper.computeMovementSequence(layer, sectorParts, characterPosition);
            }

            int characterSetIndex = SectorPart.getCharacterIndexInString(sectorParts, characterPosition);

            if (!action.getLowerCase().isEmpty()) {
                if (characterSets.getLeft().length() == 0) {
                    characterSets.getLeft().setLength(Constants.CHARACTER_SET_SIZE);
                }
                characterSets.getLeft().setCharAt(characterSetIndex, action.getLowerCase().charAt(0));

                if (action.getUpperCase().isEmpty()) {
                    action.setUpperCase(action.getLowerCase().toUpperCase(Locale.ROOT));
                }
            }

            if (!action.getUpperCase().isEmpty()) {
                if (characterSets.getRight().length() == 0) {
                    characterSets.getRight().setLength(Constants.CHARACTER_SET_SIZE);
                }
                characterSets.getRight().setCharAt(characterSetIndex, action.getUpperCase().charAt(0));
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.getActionType(), action.getLowerCase(), action.getUpperCase(),
                            action.getKeyCode(), action.getFlags(),
                            layer);

            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }
}
