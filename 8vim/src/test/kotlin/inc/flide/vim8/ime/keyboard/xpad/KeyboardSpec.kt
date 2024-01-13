package inc.flide.vim8.ime.keyboard.xpad

import androidx.compose.ui.geometry.Offset
import inc.flide.vim8.ime.layout.models.FingerPosition
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class KeyboardSpec : FunSpec({
    val circle = Keyboard.Circle(radius = 10f)

    context("is point inside circle") {
        withData(
            (Offset(1f, 1f) to true),
            (Offset(20f, 20f) to false)
        ) { (point, expected) ->
            circle.isPointInsideCircle(point) shouldBe expected
        }
    }

    context("get sector from a point") {
        withData(
            nameFn = {
                "Finger at (${it.second.x}, ${it.second.y})" +
                    "should be the ${it.first} sector"
            },
            (FingerPosition.TOP to Offset(0f, -10f)),
            (FingerPosition.LEFT to Offset(-10f, 0f)),
            (FingerPosition.BOTTOM to Offset(0f, 10f)),
            (FingerPosition.RIGHT to Offset(10f, 0f))
        ) { (position, point) ->
            circle.getSectorOfPoint(point) shouldBe position
        }
    }
})
