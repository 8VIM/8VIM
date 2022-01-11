package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.structures.FingerPosition
import kotlin.math.atan2
import kotlin.math.pow

class Circle @JvmOverloads constructor(var centre: PointF = PointF(0f, 0f), var radius: Float = 0f){

    fun setCentre(x: Float, y: Float) {
        this.centre = PointF(x, y);
    }

    private fun getPowerOfPoint(point: PointF): Double {
        /*
        If O is the centre of circle
        Consider startingPoint point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */
        val dSquare: Double = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre)
        val rSquare: Double = radius.toDouble().pow(2.0)
        return dSquare - rSquare
    }

    /**
     * Gets the angle of point p relative to the center
     * think of the regular cartisian coordinates rotated about center by 135 degree anti-clockwise
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: PointF): Double {
        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        return (atan2(point.x - 360.0, 360.0 - point.y) + (Math.PI * 2)) % (Math.PI * 2)
    }

    fun getCurrentFingerPosition(position: PointF): FingerPosition {
        return if (getPowerOfPoint(position) <= 0) {
            FingerPosition.INSIDE_CIRCLE
        } else {
            getSectorOfPoint(position)
        }
    }
    /**
     * Get the number of the sector that point p is in
     *
     * @return 0: top, 1: right, 2: bottom, 3: left
     */
    private fun getSectorOfPoint(p: PointF): FingerPosition {
        val angleDouble: Double = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
        val quadrantCyclic: Int = angleDouble.toInt() / (Math.PI / 2).toInt()
        return when (GeometricUtilities.getBaseQuadrant(quadrantCyclic)) {
            0 -> FingerPosition.TOP
            1 -> FingerPosition.RIGHT
            2 -> FingerPosition.BOTTOM
            else -> FingerPosition.LEFT //a.k.a 3
        }
    }
}