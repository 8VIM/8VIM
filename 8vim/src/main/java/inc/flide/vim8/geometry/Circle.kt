package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.structures.FingerPosition
import kotlin.math.atan2
import kotlin.math.pow

class Circle @JvmOverloads constructor(private var centre: PointF = PointF(0f, 0f), private var radius: Float = 0f){

    fun getCentre(): PointF {
        return centre
    }

    fun setCentre(centre: PointF) {
        this.centre = centre
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float) {
        this.radius = radius
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

    fun isPointInsideCircle(point: PointF): Boolean {
        return getPowerOfPoint(point) < 0
    }

    /**
     * Gets the angle of point p relative to the center
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: PointF): Double {
        // Get difference of coordinates
        val x = (point.x - centre.x).toDouble()
        val y = (centre.y - point.y).toDouble()

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        var angle = atan2(y, x)
        // Make all angles positive
        if (angle < 0) {
            angle += Math.PI * 2
        }
        return angle
    }

    /**
     * Get the number of the sector that point p is in
     *
     * @return 0: right, 1: top, 2: left, 3: bottom
     */
    fun getSectorOfPoint(p: PointF): FingerPosition? {
        val angleDouble: Double = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
        val angleToSectorValue: Double = angleDouble / (Math.PI / 2)
        val quadrantCyclic: Int = angleToSectorValue.toInt()
        when (GeometricUtilities.getBaseQuadrant(quadrantCyclic)) {
            0 -> return FingerPosition.RIGHT
            1 -> return FingerPosition.TOP
            2 -> return FingerPosition.LEFT
            3 -> return FingerPosition.BOTTOM
        }
        return null
    }
}