package inc.flide.vim8.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class DirectionSpec : FunSpec({
    context("convert a Direction to a FingerPosition") {
        withData(
            (Direction.RIGHT to FingerPosition.RIGHT),
            (Direction.LEFT to FingerPosition.LEFT),
            (Direction.TOP to FingerPosition.TOP),
            (Direction.BOTTOM to FingerPosition.BOTTOM)
        ) { (direction, fingerPosition) ->
            direction.toFingerPosition() shouldBe fingerPosition
        }
    }
    context("get the opposite direction") {
        withData(
            (Direction.RIGHT to Direction.LEFT),
            (Direction.LEFT to Direction.RIGHT),
            (Direction.TOP to Direction.BOTTOM),
            (Direction.BOTTOM to Direction.TOP)
        ) { (direction, opposite) ->
            direction.opposite() shouldBe opposite
        }
    }

    context("get a quadrant from an int") {
        withData(
            (-1 to Direction.TOP),
            (0 to Direction.RIGHT),
            (1 to Direction.TOP),
            (2 to Direction.LEFT),
            (3 to Direction.BOTTOM),
            (4 to Direction.RIGHT)
        ) { (value, quadrant) ->
            Direction.baseQuadrant(value) shouldBe quadrant
        }
    }
})
