package inc.flide.vim8.keyboardHelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

import androidx.preference.PreferenceManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;

@RunWith(MockitoJUnitRunner.class)
public class InputMethodServiceHelperTest {
    static Map<List<FingerPosition>, KeyboardAction> movementSequences;
    private static MockedStatic<KeyEvent> keyEvent;
    private static SharedPreferenceHelper sharedPreferenceHelper;
    private static MockedStatic<SharedPreferenceHelper> sharedPreferenceHelperMockedStatic;
    private static MockedStatic<Uri> uriMockedStatic;
    private static Context context;
    @Mock
    Resources resources;
    @Mock
    private SharedPreferences sharedPreferences;

    @BeforeClass
    public static void setup() {
        context = mock(Context.class);
        lenient().when(context.getPackageName()).thenReturn("package");

        keyEvent = mockStatic(KeyEvent.class);
        keyEvent.when(() -> KeyEvent.keyCodeFromString(anyString())).thenReturn(KeyEvent.KEYCODE_UNKNOWN);

        sharedPreferenceHelper = mock(SharedPreferenceHelper.class);
        when(sharedPreferenceHelper.getString(anyString(), anyString())).thenReturn("en");
        sharedPreferenceHelperMockedStatic = mockStatic(SharedPreferenceHelper.class);
        sharedPreferenceHelperMockedStatic.when(() -> SharedPreferenceHelper.getInstance(any())).thenReturn(sharedPreferenceHelper);

        uriMockedStatic = mockStatic(Uri.class);
        uriMockedStatic.when(() -> Uri.parse(anyString())).thenReturn(mock(Uri.class));
    }

    @AfterClass
    public static void close() {
        keyEvent.close();
        sharedPreferenceHelperMockedStatic.close();
        uriMockedStatic.close();
    }

    @BeforeClass
    public static void setupExpectation() {
        movementSequences = new HashMap<>();
        movementSequences.put(new ArrayList<>(Arrays.asList(FingerPosition.TOP, FingerPosition.NO_TOUCH)),
            new KeyboardAction(KeyboardActionType.INPUT_KEY, "", "", CustomKeycode.SHIFT_TOGGLE.getKeyCode(), 0, 0));
        movementSequences.put(
            new ArrayList<>(Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE)),
            new KeyboardAction(KeyboardActionType.INPUT_TEXT, "n", "N", 0, 0, 1));
    }

    @Before
    public void setupMock() {
        when(resources.getString(anyInt())).thenReturn("pref");
        lenient().when(resources.getIdentifier(anyString(), anyString(), anyString())).thenReturn(0);
        when(resources.openRawResource(anyInt())).thenAnswer((arg) -> {
            if ((int) arg.getArgument(0) == 0) {
                return getClass().getResourceAsStream("/one_layer.yaml");
            }
            return getClass().getResourceAsStream("/hidden_layer.yaml");
        });
    }

    @Test
    public void initializeKeyboardActionMap_not_using_custom_keyboard_layout() {
        when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(false);

        KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    public void initializeKeyboardActionMap_using_custom_keyboard_layout() throws FileNotFoundException {
        ContentResolver contentResolver = mock(ContentResolver.class);
        lenient().when(contentResolver.openInputStream(any())).thenReturn(getClass().getResourceAsStream("/one_layer.yaml"));
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(true);

        KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, context);

        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
    }

    @Test
    public void initializeKeyboardActionMapForCustomLayout() throws FileNotFoundException {
        try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
            preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(any())).thenReturn(sharedPreferences);

            ContentResolver contentResolver = mock(ContentResolver.class);
            lenient().when(contentResolver.openInputStream(any())).thenReturn(getClass().getResourceAsStream("/one_layer.yaml"));
            when(context.getContentResolver()).thenReturn(contentResolver);

            when(context.getString(anyInt())).thenReturn("pref");
            SharedPreferences.Editor sharedPreferencesEditor = mock(SharedPreferences.Editor.class);
            when(sharedPreferencesEditor.putBoolean(anyString(), anyBoolean())).thenReturn(sharedPreferencesEditor);
            doNothing().when(sharedPreferencesEditor).apply();
            when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
            when(sharedPreferenceHelper.getBoolean(anyString(), anyBoolean())).thenReturn(false);

            KeyboardData keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resources, context, null);

            assertThat(keyboardData.getActionMap()).containsAllEntriesOf(movementSequences);
        }
    }
}
