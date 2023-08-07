package inc.flide.vim8.models

import inc.flide.vim8.arbitraries.Arbitraries
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum

class KeyboardDataSpec : DescribeSpec({
    describe("KeyboardData") {
        it("should add all action map") {
            checkAll<Map<MovementSequence, KeyboardAction>> { keyboardActions ->
                val keyboardData = KeyboardData().addAllToActionMap(keyboardActions)
                keyboardData.actionMap shouldContainExactly keyboardActions
            }
        }
        it("should get characterSets") {
            checkAll(
                Arbitraries.arbCharactersSet,
                Exhaustive.enum<LayerLevel>()
            ) { characterSet, layer ->
                val keyboardData = KeyboardData()
                    .setLowerCaseCharacters(characterSet, layer)
                    .setUpperCaseCharacters(characterSet, layer)
                when (layer) {
                    LayerLevel.HIDDEN -> {
                        keyboardData.lowerCaseCharacters(layer).shouldBeNone()
                        keyboardData.upperCaseCharacters(layer).shouldBeNone()
                    }

                    else -> {
                        keyboardData.lowerCaseCharacters(layer) shouldBeSome characterSet
                        keyboardData.upperCaseCharacters(layer) shouldBeSome characterSet
                    }
                }
            }
        }

        it("should get the correct totalLayers") {
            checkAll(
                Arbitraries.arbCharactersSet,
                Exhaustive.enum<LayerLevel>()
            ) { characterSet, layer ->
                val keyboardData = KeyboardData().setLowerCaseCharacters(characterSet, layer)
                when (layer) {
                    LayerLevel.HIDDEN -> keyboardData.totalLayers shouldBe 0
                    else -> keyboardData.totalLayers shouldBe layer.ordinal
                }
            }
        }
    }
})