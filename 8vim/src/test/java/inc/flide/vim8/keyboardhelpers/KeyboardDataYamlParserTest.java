package inc.flide.vim8.keyboardhelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;

import android.view.KeyEvent;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.exceptions.InvalidYamlException;
import inc.flide.vim8.structures.exceptions.YamlException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KeyboardDataYamlParserTest {
    private static MockedStatic<KeyEvent> keyEvent;

    @BeforeAll
    static void setup() {
        keyEvent = Mockito.mockStatic(KeyEvent.class);
        keyEvent.when(() -> KeyEvent.keyCodeFromString(anyString())).thenAnswer(args -> {
            if (args.getArgument(0, String.class).equals("KEYCODE_A")) {
                return KeyEvent.KEYCODE_A;
            }
            return KeyEvent.KEYCODE_UNKNOWN;
        });
    }

    @AfterAll
    static void close() {
        keyEvent.close();
    }

    @Test
    void parse_valid_file() throws YamlException {
        Map<List<FingerPosition>, KeyboardAction> movementSequences = new HashMap<>();
        movementSequences.put(new ArrayList<>(Arrays.asList(FingerPosition.TOP, FingerPosition.NO_TOUCH)),
                new KeyboardAction(KeyboardActionType.INPUT_KEY, "", "", CustomKeycode.SHIFT_TOGGLE.getKeyCode(), 0,
                        0));
        movementSequences.put(new ArrayList<>(Collections.singletonList(FingerPosition.NO_TOUCH)),
                new KeyboardAction(KeyboardActionType.INPUT_KEY, "", "", KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON,
                        0));
        movementSequences.put(
                new ArrayList<>(Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM,
                        FingerPosition.INSIDE_CIRCLE)),
                new KeyboardAction(KeyboardActionType.INPUT_TEXT, "n", "N", 0, KeyEvent.META_CTRL_ON, 1));
        movementSequences.put(new ArrayList<>(
                        Arrays.asList(FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM,
                                FingerPosition.INSIDE_CIRCLE,
                                FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT,
                                FingerPosition.INSIDE_CIRCLE)),
                new KeyboardAction(KeyboardActionType.INPUT_TEXT, "m", "a", 0,
                        KeyEvent.META_CTRL_ON | KeyEvent.META_FUNCTION_ON, 2));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(Constants.CHARACTER_SET_SIZE);
        stringBuilder.setCharAt(0, 'n');

        InputStream inputStream = getClass().getResourceAsStream("/valid_file.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        KeyboardData keyboardData = parser.readKeyboardData(inputStream);
        assertThat(keyboardData.getTotalLayers()).isEqualTo(2);

        assertThat(keyboardData.getLowerCaseCharacters(Constants.DEFAULT_LAYER)).isEqualTo(stringBuilder.toString());

        stringBuilder.setCharAt(0, '\0');
        stringBuilder.setCharAt(2, 'a');

        assertThat(keyboardData.getUpperCaseCharacters(2)).isEqualTo(stringBuilder.toString());
        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    void parse_only_hidden() {
        InputStream inputStream = getClass().getResourceAsStream("/hidden_layer.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatNoException().isThrownBy(() -> parser.readKeyboardData(inputStream));
    }

    @Test
    void parse_only_default() {
        InputStream inputStream = getClass().getResourceAsStream("/one_layer.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatNoException().isThrownBy(() -> parser.readKeyboardData(inputStream));
    }

    @Test
    void parse_invalid_file_format() {
        InputStream inputStream = getClass().getResourceAsStream("/invalid_file.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatExceptionOfType(InvalidYamlException.class).isThrownBy(() -> parser.readKeyboardData(inputStream));
    }

    @Test
    void parse_invalid_extra_layers() {
        InputStream inputStream = getClass().getResourceAsStream("/extra_layers.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatExceptionOfType(InvalidYamlException.class).isThrownBy(() -> parser.readKeyboardData(inputStream));
    }

    @Test
    void parse_no_layers_format() {
        InputStream inputStream = getClass().getResourceAsStream("/no_layers.yaml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatExceptionOfType(InvalidYamlException.class).isThrownBy(() -> parser.readKeyboardData(inputStream));
    }

    @Test
    void parse_non_yaml_file() {
        InputStream inputStream = getClass().getResourceAsStream("/invalid_file.xml");
        KeyboardDataYamlParser parser = KeyboardDataYamlParser.getInstance();
        assertThatExceptionOfType(InvalidYamlException.class).isThrownBy(() -> parser.readKeyboardData(inputStream));
    }
}
