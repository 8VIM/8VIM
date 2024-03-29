package inc.flide.vim8.ime.layout.parsers

import android.view.KeyEvent
import arrow.core.Option
import arrow.core.elementAtOrNone
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.characterSets
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockkStatic

class YamlSpec : DescribeSpec({
    beforeSpec {
        mockkStatic(KeyEvent::keyCodeFromString)
        every { KeyEvent.keyCodeFromString(any()) } returns KeyEvent.KEYCODE_UNKNOWN
        every { KeyEvent.keyCodeFromString("KEYCODE_A") } returns KeyEvent.KEYCODE_A
    }

    afterSpec {
        clearStaticMockk(KeyEvent::class)
    }

    describe("Parsing a valid file") {
        it("load the correct KeyboardData") {
            val movementSequences = mapOf(
                listOf(
                    FingerPosition.TOP,
                    FingerPosition.NO_TOUCH
                ) to KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    "",
                    CustomKeycode.SHIFT_TOGGLE.keyCode,
                    0,
                    LayerLevel.HIDDEN
                ),
                listOf(FingerPosition.NO_TOUCH) to KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    "",
                    KeyEvent.KEYCODE_A,
                    KeyEvent.META_CTRL_ON,
                    LayerLevel.HIDDEN
                ),
                listOf(FingerPosition.NO_TOUCH) to KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    "",
                    KeyEvent.KEYCODE_A,
                    KeyEvent.META_CTRL_ON,
                    LayerLevel.HIDDEN
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
                    LayerLevel.FIRST
                ),
                listOf(
                    FingerPosition.LEFT,
                    FingerPosition.TOP
                ) to KeyboardAction(
                    KeyboardActionType.INPUT_TEXT,
                    "c",
                    "C",
                    0,
                    0,
                    LayerLevel.SECOND
                ),
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.RIGHT,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE
                ) to KeyboardAction(
                    KeyboardActionType.INPUT_TEXT,
                    "m",
                    "a",
                    0,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_FUNCTION_ON,
                    LayerLevel.SECOND
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
                    KeyboardActionType.INPUT_TEXT,
                    "m",
                    "a",
                    0,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_FUNCTION_ON,
                    LayerLevel.SECOND
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
                    LayerLevel.FIRST
                )
            )
            val inputStream = javaClass.getResourceAsStream("/valid_file.yaml")
            val layoutParser = YamlParser()
            val keyboardData = layoutParser.readKeyboardData(inputStream).shouldBeRight()
            keyboardData.totalLayers shouldBe 2
            keyboardData.characterSets(LayerLevel.FIRST).flatMap { it.elementAtOrNone(0) }
                .flatMap { Option.fromNullable(it) }
                .shouldBeSome().text shouldBe "n"

            val action = keyboardData.characterSets(LayerLevel.SECOND).shouldBeSome()
            action.elementAtOrNone(0).flatMap { Option.fromNullable(it) }
                .shouldBeSome().capsLockText shouldBe "C"
            action.elementAtOrNone(2).flatMap { Option.fromNullable(it) }
                .shouldBeSome().capsLockText shouldBe "a"
            keyboardData.actionMap shouldContainExactly movementSequences
        }

        it("there is only an hidden layer") {
            val inputStream = javaClass.getResourceAsStream("/hidden_layer.yaml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 0
        }

        it("there is only a default layer") {
            val inputStream = javaClass.getResourceAsStream("/one_layer.yaml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 1
        }
    }

    describe("Paring files with error") {

        it("not a valid layout") {
            val inputStream = javaClass.getResourceAsStream("/invalid_file.yaml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeLeft()
        }

        it("there is only an extra layer") {
            val inputStream = javaClass.getResourceAsStream("/extra_layers.yaml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeLeft()
        }

        it("there is no layers") {
            val inputStream = javaClass.getResourceAsStream("/no_layers.yaml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeLeft()
        }

        it("it's not a YAML file") {
            val inputStream = javaClass.getResourceAsStream("/invalid_file.xml")
            val layoutParser = YamlParser()
            layoutParser.readKeyboardData(inputStream).shouldBeLeft()
        }
    }
})
