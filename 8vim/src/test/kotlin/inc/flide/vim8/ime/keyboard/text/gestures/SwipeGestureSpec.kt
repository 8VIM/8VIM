package inc.flide.vim8.ime.keyboard.text.gestures

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import inc.flide.vim8.ime.keyboard.text.gestures.SwipeGesture.Detector.GesturePointer
import inc.flide.vim8.ime.keyboard.text.gestures.SwipeGesture.Direction
import inc.flide.vim8.lib.geometry.Pointer
import inc.flide.vim8.lib.geometry.PointerMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import kotlin.math.abs

class SwipeGestureSpec : FunSpec({
    lateinit var listener: SwipeGesture.Listener
    lateinit var event: MotionEvent
    lateinit var velocityTracker: VelocityTracker
    val pointer = GesturePointer()

    beforeSpec {
        mockkStatic(VelocityTracker::obtain)
        mockkStatic(Resources::getSystem)
        mockkConstructor(PointerMap::class)

        every { VelocityTracker.obtain() } answers { velocityTracker }
        every { Resources.getSystem() } returns mockk {
            val dm = DisplayMetrics()
            dm.density = 1f
            dm.densityDpi = DisplayMetrics.DENSITY_DEFAULT
            every { displayMetrics } returns dm
        }
    }

    beforeTest {
        listener = mockk<SwipeGesture.Listener>(relaxed = true) {
            every { onSwipe(any()) } returns true
        }
        event = mockk {
            every { getX(any()) } returns 1f
            every { getY(any()) } returns 1f
        }
        velocityTracker = mockk(relaxed = true)
        pointer.reset()
    }

    context("onTouchEvent") {
        withData(
            nameFn = { it.second },
            MotionEvent.ACTION_DOWN to "ACTION_DOWN",
            MotionEvent.ACTION_UP to "ACTION_UP"
        ) { (action, _) ->
            every {
                anyConstructed<PointerMap<GesturePointer>>().clear()
            } just Runs
            every { event.actionMasked } returns action
            val detector = SwipeGesture.Detector(listener)
            detector.onTouchEvent(event)
            verifyOrder {
                if (action == MotionEvent.ACTION_DOWN) {
                    anyConstructed<PointerMap<GesturePointer>>().clear()
                }
                velocityTracker.addMovement(event)
            }
        }
    }

    context("onTouchDown") {
        withData(listOf(pointer, null)) {
            every {
                anyConstructed<PointerMap<GesturePointer>>().add(any(), any())
            } returns it

            val detector = SwipeGesture.Detector(listener)
            detector.onTouchDown(event, pointer)

            if (it != null) {
                pointer.firstX shouldBe 1f
                pointer.firstY shouldBe 1f
            }
        }
    }

    context("onTouchMove") {
        withData(listOf(pointer, null)) {
            every {
                anyConstructed<PointerMap<GesturePointer>>().findById(any())
            } returns it

            val detector = SwipeGesture.Detector(listener)
            detector.onTouchMove(
                mockk<Pointer>(relaxed = true) { every { index } returns 1 }
            ) shouldBe (it != null)

            if (it != null) {
                pointer.index shouldBe 1
            }
        }
    }

    context("onTouchUp") {
        val sizeValues = listOf(1f, 2f)
        val sizes = sizeValues.flatMap { width ->
            sizeValues.map { height -> Size(width, height) }
        }
        val velocityValues = listOf(500f, 1000f)
        val velocities = velocityValues.flatMap { x ->
            velocityValues.map { y -> Offset(x, y) }
        }

        withData(listOf(pointer, null)) {
            withData(
                nameFn = { "Size: ${it.width}, ${it.height}" },
                sizes
            ) { size ->
                withData(
                    nameFn = { "Velocity: ${it.x}, ${it.y}" },
                    velocities
                ) { velocity ->
                    withData(Direction.entries) { direction ->
                        every {
                            anyConstructed<PointerMap<GesturePointer>>().findById(any())
                        } returns it

                        every {
                            anyConstructed<PointerMap<GesturePointer>>().removeById(any())
                        } returns true

                        every { velocityTracker.getXVelocity(any()) } returns velocity.x
                        every { velocityTracker.getYVelocity(any()) } returns velocity.y
                        val (x, y) = when (direction) {
                            Direction.UP -> 0f to -1f
                            Direction.RIGHT -> 1f to 0f
                            Direction.DOWN -> 0f to 1f
                            Direction.LEFT -> -1f to 0f
                        }
                        val matchTrigger = it != null &&
                            ((abs(x) > (size.width / 2f)) || (abs(y) > (size.height / 2f))) &&
                            (velocity.x == 1000f || velocity.y == 1000f)

                        every { event.getX(any()) } returns x
                        every { event.getY(any()) } returns y

                        val detector = SwipeGesture.Detector(listener)
                        detector.onTouchUp(event, pointer, size) shouldBe matchTrigger

                        if (matchTrigger) {
                            verify { listener.onSwipe(direction) }
                        }
                    }
                }
            }
        }
    }

    test("onTouchCancel") {
        every {
            anyConstructed<PointerMap<GesturePointer>>().removeById(any())
        } returns true
        val detector = SwipeGesture.Detector(listener)
        detector.onTouchCancel(pointer)
        verifyOrder {
            anyConstructed<PointerMap<GesturePointer>>().removeById(any())
        }
    }

    afterSpec {
        clearStaticMockk(Resources::class, VelocityTracker::class)
        clearConstructorMockk(PointerMap::class)
    }
})
