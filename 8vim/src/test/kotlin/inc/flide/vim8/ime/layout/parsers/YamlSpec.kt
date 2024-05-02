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
import inc.flide.vim8.ime.layout.parsers.yaml.YamlParser
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockkStatic

class YamlSpec : FunSpec({
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

    beforeSpec {
        mockkStatic(KeyEvent::keyCodeFromString)
        every { KeyEvent.keyCodeFromString(any()) } returns KeyEvent.KEYCODE_UNKNOWN
        every { KeyEvent.keyCodeFromString("KEYCODE_A") } returns KeyEvent.KEYCODE_A
    }

    context("Common versions") {
        context("Paring files with error") {
            test("not a valid layout") {
                val inputStream = javaClass.getResourceAsStream("/schemas/common/invalid_file.yaml")
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }

            test("there is only an extra layer") {
                val inputStream = javaClass.getResourceAsStream("/schemas/common/extra_layers.yaml")
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }

            test("it's not a YAML file") {
                val inputStream = javaClass.getResourceAsStream("/schemas/common/invalid_file.xml")
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }
        }
    }

    context("Version 2") {
        context("Parsing a valid file") {
            test("load the correct KeyboardData") {
                val inputStream = javaClass.getResourceAsStream("/schemas/version2/valid_file.yaml")
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

            test("there is only an hidden layer") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2/hidden_layer.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 0
            }

            test("there is only a default layer") {
                val inputStream = javaClass.getResourceAsStream("/schemas/version2/one_layer.yaml")
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 1
            }
        }

        context("Paring files with error") {
            test("there is no layers") {
                val inputStream = javaClass.getResourceAsStream("/schemas/version2/no_layers.yaml")
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }
            test("wrong version") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2/wrong_version.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }
        }
    }

    context("Version 2.1") {
        context("Parsing a valid file") {
            test("load the correct KeyboardData") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2_1/valid_file.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight()
            }

            test("there is only an hidden layer") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2_1/hidden_layer.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 0
            }

            test("there is only a functions layer") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2_1/functions_layer.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 0
            }

            test("there is only a default layer") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2_1/one_layer.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeRight().totalLayers shouldBe 1
            }
        }

        context("Paring files with error") {
            test("there is no layers") {
                val inputStream = javaClass.getResourceAsStream(
                    "/schemas/version2_1/no_layers.yaml"
                )
                val layoutParser = YamlParser()
                layoutParser.readKeyboardData(inputStream).shouldBeLeft()
            }
        }
    }

    afterSpec {
        clearStaticMockk(KeyEvent::class)
    }
})
