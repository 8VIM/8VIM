package inc.flide.vim8.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class QuadrantSpec : FunSpec({
    val keyboardData = KeyboardData(sectors = 4, layoutPositions = 4)
    context("get index for a character set string from quadrant when") {
        withData(
            nameFn = { "${it.first.sector}/${it.first.part} should have index: ${it.second}" },
            (Quadrant(FingerPosition.RIGHT, FingerPosition.BOTTOM) to 0),
            (Quadrant(FingerPosition.BOTTOM, FingerPosition.RIGHT) to 1),
            (Quadrant(FingerPosition.BOTTOM, FingerPosition.LEFT) to 8),
            (Quadrant(FingerPosition.LEFT, FingerPosition.BOTTOM) to 9),
            (Quadrant(FingerPosition.LEFT, FingerPosition.TOP) to 16),
            (Quadrant(FingerPosition.TOP, FingerPosition.LEFT) to 17),
            (Quadrant(FingerPosition.TOP, FingerPosition.RIGHT) to 24),
            (Quadrant(FingerPosition.RIGHT, FingerPosition.TOP) to 25)
        ) { (quadrant, index) ->
            quadrant.characterIndexInString(CharacterPosition.FIRST, keyboardData) shouldBe index
        }
    }

    context("opposite quadrant of RIGHT/BOTTOM when character position") {
        val quadrant = Quadrant(FingerPosition.RIGHT, FingerPosition.BOTTOM)
        withData(
            nameFn = { "${it.first} should be ${it.second.sector}/${it.second.part}" },
            (CharacterPosition.FIRST to Quadrant(FingerPosition.RIGHT, FingerPosition.TOP)),
            (CharacterPosition.SECOND to Quadrant(FingerPosition.BOTTOM, FingerPosition.RIGHT)),
            (CharacterPosition.THIRD to Quadrant(FingerPosition.LEFT, FingerPosition.BOTTOM)),
            (CharacterPosition.FOURTH to Quadrant(FingerPosition.TOP, FingerPosition.LEFT))
        ) { (position, opposite) ->
            quadrant.opposite(position, keyboardData) shouldBe opposite
        }
    }
})
