package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.structures.FingerPosition
import kotlin.math.PI
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
        If d = O P is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */
        val dSquare: Double = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre)
        val rSquare: Double = radius.toDouble().pow(2.0)
        return dSquare - rSquare
    }

    /**
     * Gets the angle of point p relative to the center of the circle
     * think of the regular Cartesian coordinates rotated about center by 135 degree anti-clockwise,
     * that's where 45 or (PI/4) starts, aka our top sector
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: PointF): Double {
        var theta = atan2((centre.y-point.y).toDouble(), (centre.x-point.x).toDouble())
        // atan return value between -PI to PI
        // convert theta to value between (PI/4) to (2PI + PI/4)
        if (theta < (PI/4)) {
            theta += PI * 2
        }
        return theta
    }

    fun getCurrentFingerPosition(position: PointF): FingerPosition {
        return if (getPowerOfPoint(position) <= 0) {
            FingerPosition.INSIDE_CIRCLE
        } else {
            getSectorOfPoint(position)
        }
    }

    /**
     * Get the sector that point p is in
     */
    private fun getSectorOfPoint(p: PointF): FingerPosition {
        val angleInRadians: Double = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
        return when (Math.toDegrees(angleInRadians)) {
            in 45.0..135.0 -> { FingerPosition.TOP }
            in 135.0..225.0 -> { FingerPosition.RIGHT }
            in 225.0..315.0 -> { FingerPosition.BOTTOM }
            else -> { FingerPosition.LEFT }
        }
    }
}