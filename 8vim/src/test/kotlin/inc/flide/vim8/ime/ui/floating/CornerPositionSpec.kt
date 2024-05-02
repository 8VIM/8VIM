package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.enum
import io.kotest.property.exhaustive.ints

class CornerPositionSpec : FunSpec({
    val screenSize = Size(10f, 10f)

    context("computeSize") {
        val currentSize = Size(7f, 7f)
        withData(Exhaustive.enum<CornerPosition>().values) { direction ->
            withData(nameFn = { "X: $it" }, Exhaustive.ints(-1..1).values) { deltaX ->
                val offsetX = (if (direction.isLeft) -deltaX else deltaX).toFloat()
                withData(nameFn = { "Y: $it" }, Exhaustive.ints(-1..1).values) { deltaY ->
                    val offsetY = (if (direction.isTop) -deltaY else deltaY).toFloat()
                    val offset = Offset(deltaX.toFloat(), deltaY.toFloat())
                    val expected = Size(
                        currentSize.width + offsetX,
                        currentSize.height + offsetY
                    )
                    direction.computeSize(currentSize, screenSize, offset) shouldBe expected
                }
            }
        }
    }

    context("computeOffset") {
        val offset = Offset(0f, -8f)
        val delta = Offset(1f, 1f)

        withData(Exhaustive.enum<CornerPosition>().values) { direction ->
            withData(
                nameFn = { "isWithinWidth: $it" },
                Exhaustive.boolean().values
            ) { isWithinWidth ->
                withData(
                    nameFn = { "isWithinHeight: $it" },
                    Exhaustive.boolean().values
                ) { isWithinHeight ->
                    val isWithinWidthWithDirection = direction.isLeft && isWithinWidth
                    val isWithinHeightWithDirection = direction.isTop && isWithinHeight

                    val width = if (isWithinWidthWithDirection) 7f else 9f
                    val height = if (isWithinHeightWithDirection) 7f else 9f
                    val size = Size(width, height)
                    val expected =
                        Offset(
                            if (isWithinWidthWithDirection) 1f else 0f,
                            if (isWithinHeightWithDirection) -7f else -9f
                        )

                    direction.computeOffset(offset, size, screenSize, delta) shouldBe expected
                }
            }
        }
    }

    context("toCornerPosition") {
        val padding = 2f
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            Offset.Zero to CornerPosition.TOP_LEFT,
            Offset(screenSize.width, 0f) to CornerPosition.TOP_RIGHT,
            Offset(0f, screenSize.height) to CornerPosition.BOTTOM_LEFT,
            Offset(screenSize.width, screenSize.height) to CornerPosition.BOTTOM_RIGHT,
            Offset(screenSize.width / 2f, screenSize.height / 2f) to null
        ) { (offset, position) ->
            offset.toCornerPosition(screenSize, padding) shouldBe position
        }
    }

    context("isLeft") {
        withData(
            nameFn = { "${it.first}:  ${it.second}" },
            CornerPosition.TOP_LEFT to true,
            CornerPosition.BOTTOM_LEFT to true,
            CornerPosition.BOTTOM_RIGHT to false
        ) { (position, expected) ->
            position.isLeft shouldBe expected
        }
    }

    context("isTop") {
        withData(
            nameFn = { "${it.first}:  ${it.second}" },
            CornerPosition.TOP_LEFT to true,
            CornerPosition.TOP_RIGHT to true,
            CornerPosition.BOTTOM_RIGHT to false
        ) { (position, expected) ->
            position.isTop shouldBe expected
        }
    }
})
