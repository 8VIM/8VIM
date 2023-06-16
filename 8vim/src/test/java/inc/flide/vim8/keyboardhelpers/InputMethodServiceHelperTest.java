package inc.flide.vim8.keyboardhelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.view.KeyEvent;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import inc.flide.vim8.arbitaries.KeyboardActionsEntryArbitrary;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.jqwik.api.sessions.JqwikSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InputMethodServiceHelperTest {
    static Map<List<FingerPosition>, KeyboardAction> expectedMovementSequences;
    static MockedStatic<KeyEvent> keyEvent;
    static MockedStatic<KeyboardDataYamlParser> keyboardDataYamlParserMockedStatic;
    static KeyboardDataYamlParser keyboardDataYamlParser;
    static KeyboardData keyboardDataMock;
    static SharedPreferenceHelper sharedPreferenceHelper;
    static MockedStatic<SharedPreferenceHelper> sharedPreferenceHelperMockedStatic;
    static MockedStatic<Uri> uriMockedStatic;
    static Context context;
    @Mock
    Resources resources;
    @Mock
    SharedPreferences sharedPreferences;
    private InputMethodServiceHelper inputMethodServiceHelper;

    @BeforeAll
    static void setup() {
        context = mock(Context.class);
        lenient().when(context.getPackageName()).thenReturn("package");

        keyEvent = mockStatic(KeyEvent.class);
        keyEvent.when(() -> KeyEvent.keyCodeFromString(anyString())).thenReturn(KeyEvent.KEYCODE_UNKNOWN);

        keyboardDataMock = mock(KeyboardData.class);
        keyboardDataYamlParser = mock(KeyboardDataYamlParser.class);
        lenient().when(keyboardDataYamlParser.readKeyboardData(any())).thenReturn(keyboardDataMock);

        keyboardDataYamlParserMockedStatic = mockStatic(KeyboardDataYamlParser.class);
        keyboardDataYamlParserMockedStatic.when(() -> KeyboardDataYamlParser.getInstance(any()))
                .thenReturn(keyboardDataYamlParser);

        sharedPreferenceHelper = mock(SharedPreferenceHelper.class);
        when(sharedPreferenceHelper.getString(anyString(), anyString())).thenReturn("en");
        sharedPreferenceHelperMockedStatic = mockStatic(SharedPreferenceHelper.class);
        sharedPreferenceHelperMockedStatic.when(() -> SharedPreferenceHelper.getInstance(any()))
                .thenReturn(sharedPreferenceHelper);

        uriMockedStatic = mockStatic(Uri.class);
        uriMockedStatic.when(() -> Uri.parse(anyString())).thenReturn(mock(Uri.class));
    }

    @AfterAll
    static void close() {
        keyEvent.close();
        sharedPreferenceHelperMockedStatic.close();
        uriMockedStatic.close();
        keyboardDataYamlParserMockedStatic.close();
    }

    @BeforeAll
    static void setupExpectations() {
        JqwikSession.start();
        expectedMovementSequences = generateMap();
        JqwikSession.finish();
    }

    private static Map<List<FingerPosition>, KeyboardAction> generateMap() {
        AtomicInteger index = new AtomicInteger(0);
        KeyboardActionsEntryArbitrary keyboardActionsArbitrary = new KeyboardActionsEntryArbitrary();
        try {
            return keyboardActionsArbitrary.get().sampleStream()
                    .limit(4).peek(entry -> {
                        KeyboardAction old = entry.getValue();
                        KeyboardAction keyboardAction =
                                new KeyboardAction(old.getKeyboardActionType(), old.getText(), old.getCapsLockText(),
                                        old.getKeyEventCode(), old.getKeyFlags(), index.getAndIncrement());
                        entry.setValue(keyboardAction);
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            return generateMap();
        }
    }

    @NonNull
    private static Map<List<FingerPosition>, KeyboardAction> getKeyboardActionMap(
            Set<Map.Entry<List<FingerPosition>, KeyboardAction>> entries, int layer) {
        return entries.stream().filter((e) -> e.getValue().getLayer() == layer)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @BeforeEach
    void setupMock() throws FileNotFoundException {
        lenient().when(resources.getString(anyInt())).thenReturn("pref");
        lenient().when(resources.getIdentifier(anyString(), anyString(), anyString())).thenReturn(0);
        lenient().when(resources.openRawResource(anyInt())).thenReturn(mock(InputStream.class));
        ContentResolver contentResolver = mock(ContentResolver.class);
        lenient().when(contentResolver.openInputStream(any())).thenReturn(mock(InputStream.class));
        when(context.getContentResolver()).thenReturn(contentResolver);
        Set<Map.Entry<List<FingerPosition>, KeyboardAction>> entries = expectedMovementSequences.entrySet();
        when(keyboardDataMock.getActionMap()).thenReturn(getKeyboardActionMap(entries, 0))
                .thenReturn(getKeyboardActionMap(entries, 1)).thenReturn(getKeyboardActionMap(entries, 2))
                .thenReturn(getKeyboardActionMap(entries, 3));
        for (int i = 0; i <= Constants.MAX_LAYERS; i++) {
            int layer = i;
            when(keyboardDataMock.getLowerCaseCharacters(eq(i))).thenReturn(
                    entries.stream().filter((e) -> e.getValue().getLayer() == layer).map(Map.Entry::getValue)
                            .map(KeyboardAction::getText).findFirst().orElse(""));
            when(keyboardDataMock.getUpperCaseCharacters(eq(i))).thenReturn(
                    entries.stream().filter((e) -> e.getValue().getLayer() == layer).map(Map.Entry::getValue)
                            .map(KeyboardAction::getCapsLockText).findFirst().orElse(""));
        }
        inputMethodServiceHelper = InputMethodServiceHelper.getInstance(resources);
    }

    @Test
    void initializeKeyboardActionMap_not_using_custom_keyboard_layout() {
        when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(false);
        KeyboardData keyboardData = inputMethodServiceHelper.initializeKeyboardActionMap(context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(expectedMovementSequences);
    }

    @Test
    void initializeKeyboardActionMap_using_custom_keyboard_layout() {
        when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        KeyboardData keyboardData = inputMethodServiceHelper.initializeKeyboardActionMap(context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(expectedMovementSequences);
    }

    @Test
    void initializeKeyboardActionMapForCustomLayout() throws FileNotFoundException {
        try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
            preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(any()))
                    .thenReturn(sharedPreferences);

            ContentResolver contentResolver = mock(ContentResolver.class);
            lenient().when(contentResolver.openInputStream(any()))
                    .thenReturn(getClass().getResourceAsStream("/one_layer.yaml"));
            when(context.getContentResolver()).thenReturn(contentResolver);

            when(context.getString(anyInt())).thenReturn("pref");
            SharedPreferences.Editor sharedPreferencesEditor = mock(SharedPreferences.Editor.class);
            when(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean())).thenReturn(sharedPreferencesEditor);
            doNothing().when(sharedPreferencesEditor).apply();
            when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
            when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(false);

            KeyboardData keyboardData =
                    inputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(context, null);

            assertThat(keyboardData.getActionMap()).containsAllEntriesOf(expectedMovementSequences);
        }
    }
}
