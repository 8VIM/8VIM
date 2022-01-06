package inc.flide.vim8.geometry

import android.graphics.PointF

object GeometricUtilities {
    fun getSquaredDistanceBetweenPoints(a: PointF?, b: PointF?): Double {
        val xSquare = Math.pow((a.x - b.x).toDouble(), 2.0)
        val ySquare = Math.pow((a.y - b.y).toDouble(), 2.0)
        return Math.abs(xSquare + ySquare)
    }

    fun getBaseQuadrant(continiousQuadrantValue: Int): Int {
        var result: Int
        // Calculate result with modulus operator
        result = continiousQuadrantValue % 4
        // Fix zero truncation
        if (result < 0) {
            result += 4
        }
        return result
    }

    fun findPointSpecifiedDistanceAwayInGivenDirection(startingPoint: PointF?, directionalAngleInDegree: Double, length: Double): PointF? {
        val directionalAngleInRadians = Math.toRadians(directionalAngleInDegree)
        val x = (startingPoint.x + length * Math.cos(directionalAngleInRadians)) as Int
        val y = (startingPoint.y + length * Math.sin(directionalAngleInRadians)) as Int
        return PointF(x, y)
    }
}