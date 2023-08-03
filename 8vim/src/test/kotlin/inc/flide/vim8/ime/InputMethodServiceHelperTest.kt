package inc.flide.vim8.ime

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.view.KeyEvent
import arrow.core.Option
import inc.flide.vim8.arbitaries.KeyboardActionsEntryArbitrary
import inc.flide.vim8.ime.KeyboardDataYamlParser.readKeyboardData
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.Constants
import net.jqwik.api.sessions.JqwikSession
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
class InputMethodServiceHelperTest {
    @Mock
    var resources: Resources? = null

    @Mock
    var sharedPreferences: SharedPreferences? = null
    @BeforeEach
    @Throws(FileNotFoundException::class)
    fun setupMock() {
        Mockito.lenient().`when`(resources!!.getString(ArgumentMatchers.anyInt()))
            .thenReturn("pref")
        Mockito.lenient().`when`(
            resources!!.getIdentifier(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        ).thenReturn(0)
        Mockito.lenient().`when`(resources!!.openRawResource(ArgumentMatchers.anyInt())).thenReturn(
            Mockito.mock(
                InputStream::class.java
            )
        )
        val contentResolver = Mockito.mock(ContentResolver::class.java)
        Mockito.lenient().`when`(contentResolver.openInputStream(ArgumentMatchers.any()))
            .thenReturn(
                Mockito.mock(
                    InputStream::class.java
                )
            )
        Mockito.`when`(context!!.contentResolver).thenReturn(contentResolver)
        val entries = expectedMovementSequences!!.entries
        Mockito.`when`(
            keyboardDataMock!!.actionMap
        ).thenReturn(getKeyboardActionMap(entries, 0))
            .thenReturn(getKeyboardActionMap(entries, 1))
            .thenReturn(getKeyboardActionMap(entries, 2))
            .thenReturn(getKeyboardActionMap(entries, 3))
        for (i in 0..Constants.MAX_LAYERS) {
            Mockito.`when`<Option<String>>(
                keyboardDataMock!!.lowerCaseCharacters(
                    ArgumentMatchers.eq(
                        i
                    )
                )
            ).thenReturn(
                entries.filter { it.value.layer==i }
                    entries.stream()
                        .filter { (_, value): Map.Entry<List<FingerPosition>, KeyboardAction> -> value.layer == i }
                        .map<KeyboardAction>(
                            Function<Map.Entry<List<FingerPosition>, KeyboardAction>, KeyboardAction> { (key, value) -> java.util.Map.Entry.value })
                        .map<String?>(KeyboardAction::text).findFirst().orElse(null)
                )
            )
            Mockito.`when`<Option<String>>(
                keyboardDataMock!!.upperCaseCharacters(
                    ArgumentMatchers.eq(
                        i
                    )
                )
            ).thenReturn(
                fromNullable.fromNullable<String>(
                    entries.stream()
                        .filter { (_, value): Map.Entry<List<FingerPosition>, KeyboardAction> -> value.layer == i }
                        .map<KeyboardAction>(
                            Function<Map.Entry<List<FingerPosition>, KeyboardAction>, KeyboardAction> { (key, value) -> java.util.Map.Entry.value })
                        .map<String?>(KeyboardAction::capsLockText).findFirst().orElse(null)
                )
            )
        }
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun initializeKeyboardActionMapForCustomLayout() {
        /* try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
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
                    InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resources, context, null);

            assertThat(keyboardData.getActionMap()).containsAllEntriesOf(expectedMovementSequences);
        }*/
    }

    companion object {
        var expectedMovementSequences: Map<List<FingerPosition>, KeyboardAction>? = null
        var keyEvent: MockedStatic<KeyEvent>? = null
        var keyboardDataYamlParserMockedStatic: MockedStatic<KeyboardDataYamlParser>? = null
        var keyboardDataMock: KeyboardData? = null
        var sharedPreferenceHelper: SharedPreferenceHelper? = null
        var sharedPreferenceHelperMockedStatic: MockedStatic<SharedPreferenceHelper>? = null
        var uriMockedStatic: MockedStatic<Uri>? = null
        var context: Context? = null
        @BeforeAll
        fun setup() {
            context = Mockito.mock(
                Context::class.java
            )
            Mockito.lenient().`when`(context.getPackageName()).thenReturn("package")
            keyEvent = Mockito.mockStatic(
                KeyEvent::class.java
            )
            keyEvent.`when`<Any>(Verification { KeyEvent.keyCodeFromString(ArgumentMatchers.anyString()) })
                .thenReturn(
                    KeyEvent.KEYCODE_UNKNOWN
                )
            keyboardDataMock = Mockito.mock(
                KeyboardData::class.java
            )
            keyboardDataYamlParserMockedStatic = Mockito.mockStatic(
                KeyboardDataYamlParser::class.java
            )
            keyboardDataYamlParserMockedStatic.`when`<Any?>(
                Verification { readKeyboardData(ArgumentMatchers.any()) })
                .thenReturn(keyboardDataMock)
            sharedPreferenceHelper = Mockito.mock(
                SharedPreferenceHelper::class.java
            )
            Mockito.`when`(
                sharedPreferenceHelper.getString(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString()
                )
            ).thenReturn("en")
            sharedPreferenceHelperMockedStatic = Mockito.mockStatic(
                SharedPreferenceHelper::class.java
            )
            sharedPreferenceHelperMockedStatic.`when`<Any?>(
                Verification { SharedPreferenceHelper.getInstance(ArgumentMatchers.any()) })
                .thenReturn(sharedPreferenceHelper)
            uriMockedStatic = Mockito.mockStatic(
                Uri::class.java
            )
            uriMockedStatic.`when`<Any>(Verification { Uri.parse(ArgumentMatchers.anyString()) })
                .thenReturn(
                    Mockito.mock(
                        Uri::class.java
                    )
                )
        }

        @AfterAll
        fun close() {
            keyEvent!!.close()
            sharedPreferenceHelperMockedStatic!!.close()
            uriMockedStatic!!.close()
            keyboardDataYamlParserMockedStatic!!.close()
        }

        @BeforeAll
        fun setupExpectations() {
            JqwikSession.start()
            expectedMovementSequences = generateMap()
            JqwikSession.finish()
        }

        private fun generateMap(): Map<List<FingerPosition>, KeyboardAction> {
            val index = AtomicInteger(0)
            val keyboardActionsArbitrary = KeyboardActionsEntryArbitrary()
            return try {
                keyboardActionsArbitrary.get()!!.sampleStream()
                    .limit(4)
                    .peek { entry: MutableMap.MutableEntry<List<FingerPosition?>?, KeyboardAction> ->
                        val (keyboardActionType, text, capsLockText, keyEventCode, keyFlags) = entry.value
                        val keyboardAction = KeyboardAction(
                            keyboardActionType, text, capsLockText,
                            keyEventCode, keyFlags, index.getAndIncrement()
                        )
                        entry.setValue(keyboardAction)
                    }.collect(
                        Collectors.toMap<Map.Entry<List<FingerPosition>, KeyboardAction>, List<FingerPosition>, KeyboardAction>(
                            Function<Map.Entry<List<FingerPosition>, KeyboardAction>, List<FingerPosition>> { (key, value) -> java.util.Map.Entry.key },
                            Function<Map.Entry<List<FingerPosition>, KeyboardAction>, KeyboardAction> { (key, value) -> java.util.Map.Entry.value })
                    )
            } catch (e: Exception) {
                generateMap()
            }
        }

        private fun getKeyboardActionMap(
            entries: Set<Map.Entry<List<FingerPosition>, KeyboardAction>>, layer: Int
        ): Map<List<FingerPosition>, KeyboardAction> {
            return entries.stream()
                .filter { (_, value): Map.Entry<List<FingerPosition>, KeyboardAction> -> value.layer == layer }
                .collect(
                    Collectors.toMap<Map.Entry<List<FingerPosition>, KeyboardAction>, List<FingerPosition>, KeyboardAction>(
                        Function<Map.Entry<List<FingerPosition>, KeyboardAction>, List<FingerPosition>> { (key, value) -> java.util.Map.Entry.key },
                        Function<Map.Entry<List<FingerPosition>, KeyboardAction>, KeyboardAction> { (key, value) -> java.util.Map.Entry.value })
                )
        }
    }
}