package inc.flide.vim8.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class QuadrantSpec : FunSpec({
    context("get index for a character set string from quadrant") {
        withData(
            (Quadrant(Direction.RIGHT, Direction.BOTTOM) to 0),
            (Quadrant(Direction.BOTTOM, Direction.RIGHT) to 1),
            (Quadrant(Direction.BOTTOM, Direction.LEFT) to 8),
            (Quadrant(Direction.LEFT, Direction.BOTTOM) to 9),
            (Quadrant(Direction.LEFT, Direction.TOP) to 16),
            (Quadrant(Direction.TOP, Direction.LEFT) to 17),
            (Quadrant(Direction.TOP, Direction.RIGHT) to 24),
            (Quadrant(Direction.RIGHT, Direction.TOP) to 25)
        ) { (quadrant, index) ->
            quadrant.characterIndexInString(CharacterPosition.FIRST) shouldBe index
        }
    }

    context("get opposite quadrant") {
        val quadrant = Quadrant(Direction.RIGHT, Direction.BOTTOM)
        withData(
            (CharacterPosition.FIRST to Quadrant(Direction.RIGHT, Direction.TOP)),
            (CharacterPosition.SECOND to Quadrant(Direction.BOTTOM, Direction.RIGHT)),
            (CharacterPosition.THIRD to Quadrant(Direction.LEFT, Direction.BOTTOM)),
            (CharacterPosition.FOURTH to Quadrant(Direction.TOP, Direction.LEFT)),
        ) { (position, opposite) ->
            quadrant.opposite(position) shouldBe opposite
        }
    }
})