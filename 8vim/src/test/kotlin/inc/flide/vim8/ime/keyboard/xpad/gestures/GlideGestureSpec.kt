package inc.flide.vim8.ime.keyboard.xpad.gestures

import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathMeasure
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify

class GlideGestureSpec : FunSpec({
    lateinit var listener: GlideGesture.Listener
    lateinit var event: MotionEvent
    lateinit var pathMeasure: PathMeasure
    val radius = TRAIL_MAX_RADIUS * (1 - 1f / TRAIL_STEPS)

    beforeSpec {
        mockkStatic(::PathMeasure)
        every { PathMeasure() } answers { pathMeasure }
    }

    beforeTest {
        listener = mockk<GlideGesture.Listener>(relaxed = true)
        event = mockk {
            every { x } returns 0f
            every { y } returns 0f
        }
        pathMeasure = mockk(relaxed = true) {
            every { getPosition(any()) } returns Offset.Zero
        }
    }

    context("onTouchEvent") {
        test("ACTION_DOWN") {
            val detector = GlideGesture.Detector(listener)
            every { event.actionMasked } returns MotionEvent.ACTION_DOWN
            detector.onTouchEvent(event).shouldBeTrue()
        }

        test("ACTION_MOVE") {
            val points = slot<List<GlideGesture.Point>>()
            every { listener.onTrailAddPoints(capture(points)) } just Runs
            val detector = GlideGesture.Detector(listener)
            every { pathMeasure.length } returnsMany listOf(5f, 0f)
            every { event.actionMasked } returnsMany listOf(
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE
            )
            detector.onTouchEvent(event).shouldBeTrue()
            detector.onTouchEvent(event).shouldBeTrue()
            points.captured shouldContainExactly listOf(GlideGesture.Point(radius, Offset.Zero))
        }

        test("ACTION_UP") {
            val detector = GlideGesture.Detector(listener)
            every { event.actionMasked } returns MotionEvent.ACTION_UP
            detector.onTouchEvent(event).shouldBeFalse()
            verify { listener.onTrailEnd() }
        }
    }
})
