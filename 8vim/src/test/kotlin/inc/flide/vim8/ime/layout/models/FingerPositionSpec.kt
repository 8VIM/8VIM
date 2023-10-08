package inc.flide.vim8.ime.layout.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class FingerPositionSpec : FunSpec({
    context("computing movement sequence") {
        val quadrant = Quadrant(Direction.BOTTOM, Direction.LEFT)
        withData(
            nameFn = { "for the ${it.first} layer at ${it.second} position" },
            Triple(
                LayerLevel.FIRST,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE
                )
            ),
            Triple(
                LayerLevel.FIRST,
                CharacterPosition.SECOND,
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.TOP,
                    FingerPosition.INSIDE_CIRCLE
                )
            ),
            Triple(
                LayerLevel.FIRST,
                CharacterPosition.THIRD,
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.TOP,
                    FingerPosition.RIGHT,
                    FingerPosition.INSIDE_CIRCLE
                )
            ),
            Triple(
                LayerLevel.FIRST,
                CharacterPosition.FOURTH,
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.TOP,
                    FingerPosition.RIGHT,
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE
                )
            ),
            Triple(
                LayerLevel.SECOND,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE
                )
            ),
            Triple(
                LayerLevel.THIRD,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE
                )

            ),
            Triple(
                LayerLevel.FOURTH,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.INSIDE_CIRCLE
                )

            ),
            Triple(
                LayerLevel.FIFTH,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP,
                    FingerPosition.RIGHT, FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.INSIDE_CIRCLE
                )

            ),
            Triple(
                LayerLevel.SIXTH,
                CharacterPosition.FIRST,
                listOf(
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.TOP,
                    FingerPosition.RIGHT,
                    FingerPosition.BOTTOM,
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE
                )

            )
        ) { (layer, position, result) ->
            FingerPosition.computeMovementSequence(layer, quadrant, position) shouldBe result
        }
    }
})
