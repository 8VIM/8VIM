package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.models.FingerPosition
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class CircleSpec : FunSpec({
    val circle = Circle()
    circle.radius = 10f

    fun mockPointF(x: Float, y: Float): PointF {
        val point = mockk<PointF>()
        point.x = x
        point.y = y
        return point
    }
    context("get sector from a point") {
        withData(
            (FingerPosition.TOP to mockPointF(0f, -10f)),
            (FingerPosition.LEFT to mockPointF(-10f, 0f)),
            (FingerPosition.BOTTOM to mockPointF(0f, 10f)),
            (FingerPosition.RIGHT to mockPointF(10f, 0f))
        ) { (position, point) ->
            circle.getSectorOfPoint(point) shouldBe position
        }
    }
})
