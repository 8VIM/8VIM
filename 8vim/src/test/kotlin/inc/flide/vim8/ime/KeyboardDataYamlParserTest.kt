package inc.flide.vim8.ime

import android.view.KeyEvent
import inc.flide.vim8.ime.KeyboardDataYamlParser.readKeyboardData
import inc.flide.vim8.models.error.InvalidLayoutError
import inc.flide.vim8.models.CustomKeycode
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.KeyboardActionType
import inc.flide.vim8.structures.Constants
import inc.flide.vim8.structures.exceptions.InvalidYamlException
import inc.flide.vim8.structures.exceptions.YamlException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class KeyboardDataYamlParserTest {
    @Test
    @Throws(YamlException::class)
    fun parse_valid_file() {
        val movementSequences = mapOf(
            listOf(
                FingerPosition.TOP,
                FingerPosition.NO_TOUCH
            ) to KeyboardAction(
                KeyboardActionType.INPUT_KEY, "", "", CustomKeycode.SHIFT_TOGGLE.keyCode, 0,
                0
            ),
            listOf(FingerPosition.NO_TOUCH) to KeyboardAction(
                KeyboardActionType.INPUT_KEY,
                "",
                "",
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON,
                0
            ),
            listOf(FingerPosition.NO_TOUCH) to KeyboardAction(
                KeyboardActionType.INPUT_KEY,
                "",
                "",
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON,
                0
            ),
            listOf(
                FingerPosition.INSIDE_CIRCLE,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE
            ) to KeyboardAction(
                KeyboardActionType.INPUT_TEXT,
                "n",
                "N",
                0,
                KeyEvent.META_CTRL_ON,
                1
            ),
            listOf(
                FingerPosition.LEFT,
                FingerPosition.TOP
            ) to KeyboardAction(KeyboardActionType.INPUT_TEXT, "c", "C", 0, 0, 2),
            listOf(
                FingerPosition.INSIDE_CIRCLE,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE
            ) to KeyboardAction(
                KeyboardActionType.INPUT_TEXT, "m", "a", 0,
                KeyEvent.META_CTRL_ON or KeyEvent.META_FUNCTION_ON, 2
            ),
            listOf(
                FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE,
                FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.INSIDE_CIRCLE
            ) to KeyboardAction(
                KeyboardActionType.INPUT_TEXT, "m", "a", 0,
                KeyEvent.META_CTRL_ON or KeyEvent.META_FUNCTION_ON, 2
            ),
            listOf(
                FingerPosition.INSIDE_CIRCLE, FingerPosition.RIGHT, FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE
            ) to KeyboardAction(
                KeyboardActionType.INPUT_TEXT,
                "n",
                "N",
                0,
                KeyEvent.META_CTRL_ON,
                1
            )
        )
        val stringBuilder = StringBuilder()
        stringBuilder.setLength(Constants.CHARACTER_SET_SIZE)
        stringBuilder.setCharAt(0, 'n')
        val inputStream = javaClass.getResourceAsStream("/valid_file.yaml")
        val keyboardData = readKeyboardData(inputStream).getOrNull()
        assertThat(keyboardData?.totalLayers).isEqualTo(2)
        assertThat(
            keyboardData?.lowerCaseCharacters(Constants.DEFAULT_LAYER)?.getOrNull()
        ).isEqualTo(
            stringBuilder.toString()
        )
        stringBuilder.setCharAt(0, 'C')
        stringBuilder.setCharAt(2, 'a')
        assertThat(
            keyboardData?.upperCaseCharacters(2)?.getOrNull()
        ).isEqualTo(stringBuilder.toString())
        assertThat(keyboardData?.actionMap).containsExactlyEntriesOf(movementSequences)
    }

    @Test
    fun parse_only_hidden() {
        val inputStream = javaClass.getResourceAsStream("/hidden_layer.yaml")
        Assertions.assertThatNoException().isThrownBy { readKeyboardData(inputStream) }
    }

    @Test
    fun parse_only_default() {
        val inputStream = javaClass.getResourceAsStream("/one_layer.yaml")
        Assertions.assertThatNoException().isThrownBy { readKeyboardData(inputStream) }
    }

    @Test
    fun parse_invalid_file_format() {
        val inputStream = javaClass.getResourceAsStream("/invalid_file.yaml")
        Assertions.assertThatExceptionOfType(
            InvalidYamlException::class.java
        ).isThrownBy { readKeyboardData(inputStream) }
    }

    @Test
    fun parse_invalid_extra_layers() {
        val inputStream = javaClass.getResourceAsStream("/extra_layers.yaml")
        Assertions.assertThatExceptionOfType(
            InvalidYamlException::class.java
        ).isThrownBy { readKeyboardData(inputStream) }
    }

    @Test
    fun parse_no_layers_format() {
        val inputStream = javaClass.getResourceAsStream("/no_layers.yaml")
        val errors = readKeyboardData(inputStream).leftOrNull()
        assertThat(errors).isInstanceOf(InvalidLayoutError::class.java)
    }

    @Test
    fun parse_non_yaml_file() {
        val inputStream = javaClass.getResourceAsStream("/invalid_file.xml")
        val errors = readKeyboardData(inputStream).leftOrNull()
        assertThat(errors).isInstanceOf(InvalidLayoutError::class.java)
    }

    companion object {
        private var keyEvent: MockedStatic<KeyEvent>? = null

        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            keyEvent = Mockito.mockStatic(
                KeyEvent::class.java
            )!!
            keyEvent!!.`when`<Any>(Verification { KeyEvent.keyCodeFromString(ArgumentMatchers.anyString()) })
                .thenAnswer { args: InvocationOnMock ->
                    if (args.getArgument<String>(0, String::class.java) == "KEYCODE_A") {
                        return@thenAnswer KeyEvent.KEYCODE_A
                    }
                    KeyEvent.KEYCODE_UNKNOWN
                }
        }

        @JvmStatic
        @AfterAll
        fun close(): Unit {
            keyEvent!!.close()
        }
    }
}