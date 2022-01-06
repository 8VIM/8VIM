package inc.flide.vim8.geometry

import android.graphics.PointF
import inc.flide.vim8.structures.FingerPosition

class Circle {
    private var centre: PointF?
    private var radius: Float

    constructor() {
        centre = PointF(0f, 0f)
        radius = 0f
    }

    constructor(centre: PointF?, radius: Float) {
        this.centre = centre
        this.radius = radius
    }

    fun getCentre(): PointF? {
        return centre
    }

    fun setCentre(centre: PointF?) {
        this.centre = centre
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    private fun getPowerOfPoint(point: PointF?): Double {
        /*
        If O is the centre of circle
        Consider startingPoint point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */
        val dSquare = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre)
        val rSquare = Math.pow(radius.toDouble(), 2.0)
        return dSquare - rSquare
    }

    fun isPointInsideCircle(point: PointF?): Boolean {
        return getPowerOfPoint(point) < 0
    }

    /**
     * Gets the angle of point p relative to the center
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: PointF?): Double {
        // Get difference of coordinates
        val x = (point.x - centre.x).toDouble()
        val y = (centre.y - point.y).toDouble()

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        var angle = Math.atan2(y, x)
        // Make all angles positive
        if (angle < 0) {
            angle = Math.PI * 2 + angle
        }
        return angle
    }

    /**
     * Get the number of the sector that point p is in
     *
     * @return 0: right, 1: top, 2: left, 3: bottom
     */
    fun getSectorOfPoint(p: PointF?): FingerPosition? {
        val angleDouble = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
        val angleToSectorValue = angleDouble / (Math.PI / 2)
        val quadrantCyclic = Math.round(angleToSectorValue) as Int
        val baseQuadrant = GeometricUtilities.getBaseQuadrant(quadrantCyclic)
        when (baseQuadrant) {
            0 -> return FingerPosition.RIGHT
            1 -> return FingerPosition.TOP
            2 -> return FingerPosition.LEFT
            3 -> return FingerPosition.BOTTOM
        }
        return null
    }
}