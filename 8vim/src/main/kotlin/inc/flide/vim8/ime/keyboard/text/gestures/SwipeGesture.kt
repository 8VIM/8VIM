package inc.flide.vim8.ime.keyboard.text.gestures

import android.content.res.Resources
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.compose.ui.geometry.Size
import inc.flide.vim8.lib.Pointer
import inc.flide.vim8.lib.PointerMap
import inc.flide.vim8.lib.util.ViewUtils
import kotlin.math.abs
import kotlin.math.atan2

abstract class SwipeGesture {
    class Detector(private val listener: Listener) {
        private var pointerMap: PointerMap<GesturePointer> = PointerMap { GesturePointer() }
        private val velocityTracker: VelocityTracker = VelocityTracker.obtain()
        private val thresholdSpeed =
            ViewUtils.px2dp(Resources.getSystem().displayMetrics.density * 500)

        fun onTouchEvent(event: MotionEvent) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                resetState()
            }
            velocityTracker.addMovement(event)
        }

        fun onTouchDown(event: MotionEvent, pointer: Pointer) {
            pointerMap.add(pointer.id, pointer.index)?.let { gesturePointer ->
                gesturePointer.firstX = ViewUtils.px2dp(event.getX(pointer.index))
                gesturePointer.firstY = ViewUtils.px2dp(event.getY(pointer.index))
                gesturePointer.lastX = gesturePointer.firstX
                gesturePointer.lastY = gesturePointer.firstY
            }
        }

        fun onTouchMove(event: MotionEvent, pointer: Pointer): Boolean {
            pointerMap.findById(pointer.id)?.let { gesturePointer ->
                gesturePointer.index = pointer.index
                val currentX = ViewUtils.px2dp(event.getX(pointer.index))
                val currentY = ViewUtils.px2dp(event.getY(pointer.index))
                gesturePointer.lastX = currentX
                gesturePointer.lastY = currentY

                return true
            }
            return false
        }

        fun onTouchUp(event: MotionEvent, pointer: Pointer, size: Size): Boolean {
            pointerMap.findById(pointer.id)?.let { gesturePointer ->
                val currentX = ViewUtils.px2dp(event.getX(pointer.index))
                val currentY = ViewUtils.px2dp(event.getY(pointer.index))
                val absDiffX = currentX - gesturePointer.firstX
                val absDiffY = currentY - gesturePointer.firstY
                velocityTracker.computeCurrentVelocity(1000)
                val velocityX = ViewUtils.px2dp(velocityTracker.getXVelocity(pointer.id))
                val velocityY = ViewUtils.px2dp(velocityTracker.getYVelocity(pointer.id))
                pointerMap.removeById(pointer.id)
                return if ((
                    abs(absDiffX) > (ViewUtils.px2dp(size.width) / 2.0) || abs(absDiffY) > (
                        ViewUtils.px2dp(
                                size.height
                            ) / 2.0
                        )
                    ) && (
                        abs(
                                velocityX
                            ) > thresholdSpeed || abs(velocityY) > thresholdSpeed
                        )
                ) {
                    val direction = detectDirection(absDiffX.toDouble(), absDiffY.toDouble())
                    listener.onSwipe(direction)
                } else {
                    false
                }
            }
            return false
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
                diffAngle >= (7 / 16.0) && diffAngle < (9 / 16.0) -> Direction.LEFT
                diffAngle >= (9 / 16.0) && diffAngle < (15 / 16.0) -> Direction.UP
                else -> Direction.RIGHT
            }
        }

        private fun resetState() {
            pointerMap.clear()
        }

        class GesturePointer : Pointer() {
            var firstX: Float = 0.0f
            var firstY: Float = 0.0f
            var lastX: Float = 0.0f
            var lastY: Float = 0.0f

            override fun reset() {
                super.reset()
                firstX = 0.0f
                firstY = 0.0f
                lastX = 0.0f
                lastY = 0.0f
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
