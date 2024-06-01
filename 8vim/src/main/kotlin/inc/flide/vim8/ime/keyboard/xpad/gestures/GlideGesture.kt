package inc.flide.vim8.ime.keyboard.xpad.gestures

import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure

internal const val TRAIL_STEPS = 150
internal const val TRAIL_STEP_DISTANCE = 5
internal const val TRAIL_MAX_RADIUS = 14

object GlideGesture {
    class Detector(private val listener: Listener) {
        private val typingTrailPath = Path()
        private val pathMeasure = PathMeasure()

        fun onTouchEvent(event: MotionEvent): Boolean {
            return when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    typingTrailPath.reset()
                    typingTrailPath.moveTo(event.x, event.y)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    typingTrailPath.lineTo(event.x, event.y)
                    points()
                    true
                }

                else -> {
                    typingTrailPath.reset()
                    listener.onTrailEnd()
                    false
                }
            }
        }

        private fun points() {
            pathMeasure.setPath(typingTrailPath, false)
            val points = mutableListOf<Point>()
            for (i in 1..TRAIL_STEPS) {
                val distance = pathMeasure.length - i * TRAIL_STEP_DISTANCE
                if (distance >= 0) {
                    val trailRadius = TRAIL_MAX_RADIUS * (1 - i.toFloat() / TRAIL_STEPS)
                    val center = pathMeasure.getPosition(distance)
                    points.add(Point(trailRadius, center))
                }
            }
            listener.onTrailAddPoints(points)
        }
    }

    data class Point(val radius: Float, val center: Offset)
    interface Listener {
        fun onTrailAddPoints(points: List<Point>)
        fun onTrailEnd()
    }
}
