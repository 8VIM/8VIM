package inc.flide.vim8.keyboardhelpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.exceptions.InvalidYamlException;
import inc.flide.vim8.structures.exceptions.YamlException;
import inc.flide.vim8.structures.exceptions.YamlParsingException;
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
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class KeyboardDataYamlParser {
    private static final ObjectMapper mapper =
            YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
    private static KeyboardDataYamlParser singleton = null;
    private final JsonSchema schema;

    private KeyboardDataYamlParser(JsonSchema schema) {
        this.schema = schema;
    }

    public static KeyboardDataYamlParser getInstance(InputStream schemaInputStream) {
        if (singleton == null) {
            try {
                JsonNode schemaJson = mapper.readTree(schemaInputStream);
                JsonSchemaFactory factory =
                        JsonSchemaFactory.builder(
                                        JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaJson)))
                                .objectMapper(mapper)
                                .build();
                JsonSchema schema = factory.getSchema(schemaJson);
                singleton = new KeyboardDataYamlParser(schema);
            } catch (IOException e) {
                throw new YamlParsingException(e);
            } catch (JsonSchemaException e) {
                throw new InvalidYamlException(e.getMessage());
            }
        }
        return singleton;
    }

    public KeyboardData readKeyboardData(InputStream inputStream) throws YamlException {
        try {
            JsonNode node = mapper.readTree(inputStream);
            checkSchema(node);
            Layout layout = mapper.treeToValue(node, Layout.class);
            KeyboardData keyboardData = new KeyboardData();
            keyboardData.setInfo(layout.info);

            if (!layout.layers.hidden.isEmpty()) {
                addKeyboardActions(keyboardData, layout.layers.hidden);
            }

            Layer defaultLayer = layout.layers.defaultLayer;
            Map<ExtraLayer, Layer> extraLayers = layout.layers.extraLayers;

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
        } catch (IOException exception) {
            throw new YamlParsingException(exception);
        }
    }

    private void checkSchema(JsonNode node) {
        Set<ValidationMessage> errors = schema.validate(node);
        if (!errors.isEmpty()) {
            throw new InvalidYamlException(errors);
        }
    }

    private void addLayer(KeyboardData keyboardData, int layer, Layer layerData) {
        StringBuilder lowerCaseCharacters = new StringBuilder();
        StringBuilder upperCaseCharacters = new StringBuilder();
        Pair<StringBuilder, StringBuilder> characterSets =
                Pair.of(lowerCaseCharacters, upperCaseCharacters);

        for (Map.Entry<Direction, Part> sectorEntry : layerData.sectors.entrySet()) {
            Direction sector = sectorEntry.getKey();

            for (Map.Entry<Direction, List<Action>> partEntry : sectorEntry.getValue().parts.entrySet()) {
                Direction part = partEntry.getKey();
                Quadrant quadrant = new Quadrant(sector, part);
                addKeyboardActions(keyboardData, layer, quadrant, partEntry.getValue(), characterSets);
            }
        }

        keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
        keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
    }

    private void addKeyboardActions(KeyboardData keyboardData, List<Action> actions) {
        for (Action action : actions) {
            List<FingerPosition> movementSequence = action.movementSequence;

            if (movementSequence.isEmpty()) {
                continue;
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.actionType, action.lowerCase, action.upperCase,
                            action.getKeyCode(), action.flags,
                            Constants.HIDDEN_LAYER);
            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }

    private void addKeyboardActions(KeyboardData keyboardData, int layer, Quadrant quadrant,
                                    List<Action> actions,
                                    Pair<StringBuilder, StringBuilder> characterSets) {
        int actionsSize = Math.min(actions.size(), 4);

        for (int i = 0; i < actionsSize; i++) {
            Action action = actions.get(i);
            if (action == null || action.isEmpty()) {
                continue;
            }

            CharacterPosition characterPosition = CharacterPosition.values()[i];

            List<FingerPosition> movementSequence = action.movementSequence;

            if (movementSequence.isEmpty()) {
                movementSequence =
                        MovementSequenceHelper.computeMovementSequence(layer, quadrant, characterPosition);
            }

            int characterSetIndex = quadrant.getCharacterIndexInString(characterPosition);

            if (!action.lowerCase.isEmpty()) {
                if (characterSets.getLeft().length() == 0) {
                    characterSets.getLeft().setLength(Constants.CHARACTER_SET_SIZE);
                }
                characterSets.getLeft().setCharAt(characterSetIndex, action.lowerCase.charAt(0));

                if (action.upperCase.isEmpty()) {
                    action.upperCase = action.lowerCase.toUpperCase(Locale.ROOT);
                }
            }

            if (!action.upperCase.isEmpty()) {
                if (characterSets.getRight().length() == 0) {
                    characterSets.getRight().setLength(Constants.CHARACTER_SET_SIZE);
                }
                characterSets.getRight().setCharAt(characterSetIndex, action.upperCase.charAt(0));
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.actionType, action.lowerCase, action.upperCase,
                            action.getKeyCode(), action.flags,
                            layer);

            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }
}
