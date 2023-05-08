package inc.flide.vim8.keyboardHelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.KeyEvent;

import com.fasterxml.jackson.databind.DatabindException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;

@ExtendWith(MockitoExtension.class)
public class KeyboardDataYamlParserTest {
    @BeforeAll
    static void setup() {
        MockedStatic<KeyEvent> keyEvent = Mockito.mockStatic(KeyEvent.class);
        keyEvent.when(() -> KeyEvent.keyCodeFromString(anyString())).thenReturn(KeyEvent.KEYCODE_UNKNOWN);
    }

    @Test
    void parse_valid_file() throws IOException {
        Map<List<FingerPosition>, KeyboardAction> movementSequences = new HashMap<>();
        movementSequences.put(new ArrayList<>(Arrays.asList(FingerPosition.TOP, FingerPosition.NO_TOUCH)),
            new KeyboardAction(KeyboardActionType.INPUT_KEY, "", "", CustomKeycode.SHIFT_TOGGLE.getKeyCode(), 0, 0));
        movementSequences.put(
            new ArrayList<>(Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE)),
            new KeyboardAction(KeyboardActionType.INPUT_TEXT, "n", "N", 0, 0, 1));
        movementSequences.put(new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE)), new KeyboardAction(KeyboardActionType.INPUT_TEXT, "m", "a", 0, 0, 2));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(Constants.CHARACTER_SET_SIZE);
        stringBuilder.setCharAt(0, 'n');

        InputStream inputStream = getClass().getResourceAsStream("/valid_file.yaml");
        KeyboardDataYamlParser parser = new KeyboardDataYamlParser(inputStream);
        KeyboardData keyboardData = parser.readKeyboardData();
        assertThat(keyboardData.getTotalLayers()).isEqualTo(2);

        assertThat(keyboardData.getLowerCaseCharacters(Constants.DEFAULT_LAYER)).isEqualTo(stringBuilder.toString());

        stringBuilder.setCharAt(0, '\0');
        stringBuilder.setCharAt(2, 'a');

        assertThat(keyboardData.getUpperCaseCharacters(2)).isEqualTo(stringBuilder.toString());
        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    void parse_invalid_file_format() {
        InputStream inputStream = getClass().getResourceAsStream("/invalid_file.yaml");
        KeyboardDataYamlParser parser = new KeyboardDataYamlParser(inputStream);
        assertThatExceptionOfType(DatabindException.class)
            .isThrownBy(parser::readKeyboardData);
    }

    @Test
    void parse_non_yaml_file() {
        InputStream inputStream = getClass().getResourceAsStream("/invalid_file.xml");
        KeyboardDataYamlParser parser = new KeyboardDataYamlParser(inputStream);
        assertThatExceptionOfType(DatabindException.class)
            .isThrownBy(parser::readKeyboardData);
    }

    @Test
    void isValidFile_using_URI() throws FileNotFoundException {
        InputStream inputStream = getClass().getResourceAsStream("/valid_file.yaml");

        Context context = mock(Context.class);
        ContentResolver contentResolver = mock(ContentResolver.class);
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(contentResolver.openInputStream(any())).thenReturn(inputStream);
        assertThat(KeyboardDataYamlParser.isValidFile(context, Uri.EMPTY)).isEqualTo(2);
    }

    @Test
    void isValidFile_using_resources() throws FileNotFoundException {
        InputStream inputStream = getClass().getResourceAsStream("/valid_file.yaml");
        Resources resources = mock(Resources.class);
        when(resources.openRawResource(anyInt())).thenReturn(inputStream);
        assertThat(KeyboardDataYamlParser.isValidFile(resources, 0)).isEqualTo(2);
    }
}
