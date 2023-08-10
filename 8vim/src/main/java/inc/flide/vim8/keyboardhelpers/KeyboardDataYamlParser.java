package inc.flide.vim8.keyboardhelpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.exceptions.InvalidYamlException;
import inc.flide.vim8.structures.exceptions.YamlException;
import inc.flide.vim8.structures.exceptions.YamlParsingException;
import inc.flide.vim8.structures.yaml.Action;
import inc.flide.vim8.structures.yaml.ExtraLayer;
import inc.flide.vim8.structures.yaml.Flags;
import inc.flide.vim8.structures.yaml.Layer;
import inc.flide.vim8.structures.yaml.Layout;
import inc.flide.vim8.structures.yaml.Part;
import inc.flide.vim8.utils.MovementSequenceHelper;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class KeyboardDataYamlParser {
    private static final SimpleModule module =
            new SimpleModule().addDeserializer(Flags.class, new Flags.FlagsDeserializer());
    private static final ObjectMapper mapper =
            YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .addModule(module).build();
    private static final JsonSchema schema;

    static {
        try (InputStream schemaInputStream = KeyboardDataYamlParser.class.getResourceAsStream("/schema.json")) {
            JsonNode schemaJson = mapper.readTree(schemaInputStream);
            JsonSchemaFactory factory =
                    JsonSchemaFactory.builder(
                                    JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaJson)))
                            .objectMapper(mapper)
                            .build();
            schema = factory.getSchema(schemaJson);
        } catch (IOException e) {
            throw new YamlParsingException(e);
        } catch (JsonSchemaException e) {
            throw new InvalidYamlException(e.getMessage());
        }
    }

    public static KeyboardData readKeyboardData(InputStream inputStream) throws YamlException {
        try {
            JsonNode node = mapper.readTree(inputStream);
            validateYaml(node);
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

            // Find the longest action chain to suze the keyboard
            if (defaultLayer != null) {
                for (Map.Entry<Integer, Part> sectorEntry : defaultLayer.sectors.entrySet()) {
                    int sector = sectorEntry.getKey();
                    keyboardData.sectors = Math.max(keyboardData.sectors, sector);
                    for (Map.Entry<Integer, List<Action>> partEntry : sectorEntry.getValue().parts.entrySet()) {
                        keyboardData.layoutPositions = Math.max(keyboardData.layoutPositions, partEntry.getValue().size());
                    }
                }
            }
            for (Map.Entry<ExtraLayer, Layer> extraLayerMapEntry : extraLayers.entrySet()) {
                for (Map.Entry<Integer, Part> sectorEntry : extraLayerMapEntry.getValue().sectors.entrySet()) {
                    int sector = sectorEntry.getKey();
                    keyboardData.sectors = Math.max(keyboardData.sectors, sector);
                    for (Map.Entry<Integer, List<Action>> partEntry : sectorEntry.getValue().parts.entrySet()) {
                        keyboardData.layoutPositions = Math.max(keyboardData.layoutPositions, partEntry.getValue().size());
                    }
                }
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

    private static void validateYaml(JsonNode node) {
        Set<ValidationMessage> result = schema.validate(node);
        if (!result.isEmpty()) {
            Set<ValidationMessage> errors = new HashSet<>();
            for (ValidationMessage error : result) {
                if (error.getMessage().startsWith("$")) {
                    errors.add(error);
                } else {
                    errors.add(new ValidationMessage.Builder()
                            .type(error.getType())
                            .code(error.getCode())
                            .path(error.getPath())
                            .details(error.getDetails())
                            .arguments(error.getMessage())
                            .format(new MessageFormat("{0}: {1}"))
                            .build());
                }
            }
            throw new InvalidYamlException(errors);
        }
    }

    private static void addLayer(KeyboardData keyboardData, int layer, Layer layerData) {
        StringBuilder lowerCaseCharacters = new StringBuilder();
        StringBuilder upperCaseCharacters = new StringBuilder();
        Pair<StringBuilder, StringBuilder> characterSets =
                Pair.of(lowerCaseCharacters, upperCaseCharacters);

        for (Map.Entry<Integer, Part> sectorEntry : layerData.sectors.entrySet()) {
            int sector = sectorEntry.getKey();

            for (Map.Entry<Integer, List<Action>> partEntry : sectorEntry.getValue().parts.entrySet()) {
                int part = partEntry.getKey();
                Quadrant quadrant = new Quadrant(sector, part);
                addKeyboardActions(keyboardData, layer, quadrant, partEntry.getValue(), characterSets);
            }
        }

        keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
        keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
    }

    private static void addKeyboardActions(KeyboardData keyboardData, List<Action> actions) {
        for (Action action : actions) {
            List<Integer> movementSequence = action.movementSequence;

            if (movementSequence.isEmpty()) {
                continue;
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.actionType, action.lowerCase, action.upperCase,
                            action.getKeyCode(), action.flags.getValue(),
                            Constants.HIDDEN_LAYER);
            keyboardData.addActionMap(movementSequence, actionMap);
        }
    }

    private static int getCharacterSetSize(KeyboardData keyboardData) {
        return keyboardData.sectors * 2 * keyboardData.layoutPositions;
    }

    private static void addKeyboardActions(KeyboardData keyboardData, int layer, Quadrant quadrant,
                                           List<Action> actions,
                                           Pair<StringBuilder, StringBuilder> characterSets) {
        int actionsSize = Math.min(actions.size(), 4);

        for (int i = 0; i < actionsSize; i++) {
            Action action = actions.get(i);
            if (action == null || action.isEmpty()) {
                continue;
            }

            int characterPosition = i;

            List<Integer> movementSequence = action.movementSequence;

            if (movementSequence.isEmpty()) {
                movementSequence =
                        MovementSequenceHelper.computeMovementSequence(layer, quadrant, characterPosition, keyboardData);
            }

            int characterSetIndex = quadrant.getCharacterIndexInString(characterPosition, keyboardData);

            if (!action.lowerCase.isEmpty()) {
                if (characterSets.getLeft().length() == 0) {
                    characterSets.getLeft().setLength(getCharacterSetSize(keyboardData));
                }
                characterSets.getLeft().setCharAt(characterSetIndex, action.lowerCase.charAt(0));

                if (action.upperCase.isEmpty()) {
                    action.upperCase = action.lowerCase.toUpperCase(Locale.ROOT);
                }
            }

            if (!action.upperCase.isEmpty()) {
                if (characterSets.getRight().length() == 0) {
                    characterSets.getRight().setLength(getCharacterSetSize(keyboardData));
                }
                characterSets.getRight().setCharAt(characterSetIndex, action.upperCase.charAt(0));
            }

            KeyboardAction actionMap =
                    new KeyboardAction(action.actionType, action.lowerCase, action.upperCase,
                            action.getKeyCode(), action.flags.getValue(),
                            layer);

            keyboardData.addActionMap(movementSequence, actionMap);
            if (layer > Constants.DEFAULT_LAYER && action.movementSequence.isEmpty()) {
                keyboardData.addActionMap(
                        MovementSequenceHelper.computeQuickMovementSequence(layer, quadrant, characterPosition, keyboardData),
                        actionMap);
            }
        }
    }
}
