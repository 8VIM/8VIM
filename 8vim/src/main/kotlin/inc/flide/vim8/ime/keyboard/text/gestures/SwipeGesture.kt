package inc.flide.vim8.ime.keyboard.text.gestures

import android.content.res.Resources
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.compose.ui.geometry.Size
import inc.flide.vim8.lib.geometry.Pointer
import inc.flide.vim8.lib.geometry.PointerMap
import inc.flide.vim8.lib.geometry.px2dp
import kotlin.math.abs
import kotlin.math.atan2

abstract class SwipeGesture {
    class Detector(private val listener: Listener) {
        private val pointerMap: PointerMap<GesturePointer> = PointerMap { GesturePointer() }
        private val velocityTracker: VelocityTracker = VelocityTracker.obtain()
        private val thresholdSpeed =
            (Resources.getSystem().displayMetrics.density * 500).px2dp()

        fun onTouchEvent(event: MotionEvent) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                resetState()
            }
            velocityTracker.addMovement(event)
        }

        fun onTouchDown(event: MotionEvent, pointer: Pointer) {
            pointerMap.add(pointer.id, pointer.index).onSome { gesturePointer ->
                gesturePointer.firstX = event.getX(pointer.index).px2dp()
                gesturePointer.firstY = event.getY(pointer.index).px2dp()
            }
        }

        fun onTouchMove(pointer: Pointer): Boolean {
            pointerMap.findById(pointer.id).onSome { gesturePointer ->
                gesturePointer.index = pointer.index
                return true
            }
            return false
        }

        fun onTouchUp(event: MotionEvent, pointer: Pointer, size: Size): Boolean {
            return pointerMap.findById(pointer.id).filter { gesturePointer ->
                val currentX = event.getX(pointer.index).px2dp()
                val currentY = event.getY(pointer.index).px2dp()
                val absDiffX = currentX - gesturePointer.firstX
                val absDiffY = currentY - gesturePointer.firstY
                velocityTracker.computeCurrentVelocity(1000)
                val velocityX = velocityTracker.getXVelocity(pointer.id).px2dp()
                val velocityY = velocityTracker.getYVelocity(pointer.id).px2dp()
                pointerMap.removeById(pointer.id)
                if (
                    (
                        abs(absDiffX) > (size.width.px2dp() / 2.0) ||
                            abs(absDiffY) > (size.height.px2dp() / 2.0)
                        ) && (
                        abs(velocityX) > thresholdSpeed ||
                            abs(velocityY) > thresholdSpeed
                        )
                ) {
                    val direction = detectDirection(absDiffX.toDouble(), absDiffY.toDouble())
                    listener.onSwipe(direction)
                } else {
                    false
                }
            }.isSome()
        }

        fun onTouchCancel(pointer: Pointer) {
            pointerMap.removeById(pointer.id)
        }

        private fun angle(diffX: Double, diffY: Double): Double {
            return (Math.toDegrees(atan2(diffY, diffX)) + 360) % 360
        }

        private fun detectDirection(diffX: Double, diffY: Double): Direction {
            val diffAngle = angle(diffX, diffY) / 360.0
            return when {
                diffAngle >= (1 / 16.0) && diffAngle < (7 / 16.0) -> Direction.DOWN
                diffAngle >= (7 / 16.0) && diffAngle < (11 / 16.0) -> Direction.LEFT
                diffAngle >= (11 / 16.0) && diffAngle < (15 / 16.0) -> Direction.UP
                else -> Direction.RIGHT
            }
        }

        private fun resetState() {
            pointerMap.clear()
        }

        class GesturePointer : Pointer() {
            var firstX: Float = 0.0f
            var firstY: Float = 0.0f

            override fun reset() {
                super.reset()
                firstX = 0.0f
                firstY = 0.0f
            }
        }
    }

    interface Listener {
        fun onSwipe(direction: Direction): Boolean
    }

    enum class Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT;
    }
}
