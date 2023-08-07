package inc.flide.vim8.utils

import android.graphics.PointF
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class GeometricUtilitiesSpec : FunSpec({
    fun mockPointF(x: Float, y: Float): PointF {
        val point = mockk<PointF>()
        point.x = x
        point.y = y
        return point
    }

    test("square distance between two points") {
        val a = mockPointF(2f, 2f)
        val b = mockPointF(0f, 0f)
        val distance = GeometricUtilities.getSquaredDistanceBetweenPoints(a, b)
        distance shouldBe 8
    }
})
