package inc.flide.vim8.keyboardHelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.KeyEvent;

import androidx.preference.PreferenceManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;

@ExtendWith(MockitoExtension.class)
public class InputMethodServiceHelperTest {
    static SharedPreferences sharedPreferences;
    static Context context;
    static Map<List<FingerPosition>, KeyboardAction> movementSequences;
    @Mock
    Resources resources;

    @BeforeAll
    static void setup() {
        sharedPreferences = mock(SharedPreferences.class);
        context = mock(Context.class);

        lenient().when(context.getPackageName()).thenReturn("package");

        MockedStatic<KeyEvent> keyEvent = Mockito.mockStatic(KeyEvent.class);
        keyEvent.when(() -> KeyEvent.keyCodeFromString(anyString())).thenReturn(KeyEvent.KEYCODE_UNKNOWN);
        MockedStatic<PreferenceManager> preferenceManager = Mockito.mockStatic(PreferenceManager.class);
        preferenceManager.when(() -> PreferenceManager.getDefaultSharedPreferences(any())).thenReturn(sharedPreferences);
    }

    @BeforeAll
    static void setupExpectation() {
        movementSequences = new HashMap<>();
        movementSequences.put(new ArrayList<>(Arrays.asList(FingerPosition.TOP, FingerPosition.NO_TOUCH)),
            new KeyboardAction(KeyboardActionType.INPUT_KEY, "", "", CustomKeycode.SHIFT_TOGGLE.getKeyCode(), 0, 0));
        movementSequences.put(new ArrayList<>(
                Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE)),
            new KeyboardAction(KeyboardActionType.INPUT_TEXT, "n", "N", 0, 0, 1));
    }

    @BeforeEach
    void setupMock() {
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("en");
        when(resources.getString(anyInt())).thenReturn("pref");
        when(resources.getIdentifier(anyString(), anyString(), anyString())).thenReturn(0);
        when(resources.openRawResource(anyInt())).thenAnswer((arg) -> {
            if ((int) arg.getArgument(0) == 0) {
                return getClass().getResourceAsStream("/one_layer.yaml");
            }
            return getClass().getResourceAsStream("/hidden_layer.yaml");
        });
    }

    @Test
    @DisplayName("Initialize KeyboardActionMap when using a built-in layout")
    void initializeKeyboardActionMap_not_using_custom_keyboard_layout() {
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(false);

        KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    @DisplayName("Initialize KeyboardActionMap when using a custom layout")
    void initializeKeyboardActionMap_using_custom_keyboard_layout() {
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true);

        KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    @DisplayName("Initialize KeyboardActionMap for a custom layout")
    void initializeKeyboardActionMapForCustomLayout() {
        when(context.getString(anyInt())).thenReturn("pref");
        SharedPreferences.Editor sharedPreferencesEditor = mock(SharedPreferences.Editor.class);
        when(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean())).thenReturn(sharedPreferencesEditor);
        doNothing().when(sharedPreferencesEditor).apply();
        when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(false);

        KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resources, context, null);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }
}
