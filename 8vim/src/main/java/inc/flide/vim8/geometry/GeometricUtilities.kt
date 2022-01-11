package inc.flide.vim8.geometry

import android.graphics.PointF
import kotlin.math.*

object GeometricUtilities {
    fun getSquaredDistanceBetweenPoints(a: PointF, b: PointF): Double {
        val xSquare = (a.x - b.x).toDouble().pow(2.0)
        val ySquare = (a.y - b.y).toDouble().pow(2.0)
        return abs(xSquare + ySquare)
    }

    fun findPointSpecifiedDistanceAwayInGivenDirection(startingPoint: PointF, directionalAngleInDegree: Double, length: Double): PointF {
        val directionalAngleInRadians = Math.toRadians(directionalAngleInDegree)
        val x = (startingPoint.x + length * cos(directionalAngleInRadians)).toFloat().roundToInt()
        val y = (startingPoint.y + length * sin(directionalAngleInRadians)).toFloat().roundToInt()
        return PointF(x.toFloat(), y.toFloat())
    }
}