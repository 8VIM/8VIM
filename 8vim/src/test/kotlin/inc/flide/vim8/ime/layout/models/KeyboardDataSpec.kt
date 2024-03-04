package inc.flide.vim8.ime.layout.models

import inc.flide.vim8.arbitraries.Arbitraries
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.kotest.property.withAssumptions

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
                withAssumptions(characterSet.any { it != null }) {
                    val keyboardData = KeyboardData()
                        .setCharacterSets(characterSet, layer)
                    when (layer) {
                        LayerLevel.HIDDEN -> {
                            keyboardData.characterSets(layer).shouldBeNone()
                        }

                        else -> {
                            keyboardData.characterSets(layer) shouldBeSome characterSet
                        }
                    }
                }
            }
        }

        it("should get the correct totalLayers") {
            checkAll(
                Arbitraries.arbCharactersSet,
                Exhaustive.enum<LayerLevel>()
            ) { characterSet, layer ->
                withAssumptions(characterSet.any { it != null }) {
                    val keyboardData = KeyboardData().setCharacterSets(characterSet, layer)
                    when (layer) {
                        LayerLevel.HIDDEN -> keyboardData.totalLayers shouldBe 0
                        else -> keyboardData.totalLayers shouldBe layer.ordinal
                    }
                }
            }
        }
    }
})
