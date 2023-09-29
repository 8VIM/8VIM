package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.utils.GeometricUtilities
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt

class Circle {
    @JvmField
    var centre: PointF = PointF(0f, 0f)

    @JvmField
    var radius: Float = 0f

    private fun getPowerOfPoint(point: PointF): Double {
        /*
        If O is the centre of circle
        Consider startingPoint point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O,
        then the power of the point P relative to the circle is
        p=d^2-r^2.
        */
        val squaredDistanceBetweenPoints =
            GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre)
        val radiusSquare = radius.toDouble().pow(2.0)
        return squaredDistanceBetweenPoints - radiusSquare
    }

    fun isPointInsideCircle(point: PointF): Boolean {
        return getPowerOfPoint(point) < 0
    }

    /**
     * Gets the angle of point p relative to the center
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(
        point: PointF,
        keyboardData: KeyboardData
    ): Double {
        // Get difference of coordinates
        val x = (point.x - centre.x).toDouble()
        val y = (centre.y - point.y).toDouble()

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        var angle = atan2(y, x)

        val sectorsAngle = Math.PI * 2 / keyboardData.sectors / 2
        val angleCenterFirstSector = -Math.PI / 2 + sectorsAngle - sectorsAngle
        angle -= angleCenterFirstSector
        // Make all angles positive
        if (angle < 0) {
            angle += Math.PI * 2
        }
        return angle
    }

    /**
     * Get the number of the sector that point p is in
     */
    fun getSectorOfPoint(p: PointF, keyboardData: KeyboardData): Int {
        val angleDouble = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p, keyboardData)
        var quadrantCyclic = (-angleDouble / (Math.PI * 2 / keyboardData.sectors)).roundToInt()
        quadrantCyclic %= keyboardData.sectors
        if (quadrantCyclic < 0) {
            quadrantCyclic += keyboardData.sectors
        }
        return quadrantCyclic + 1
    }
}
