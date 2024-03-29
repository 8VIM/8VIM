package inc.flide.vim8.ime.keyboard.xpad

import android.content.Context
import android.util.DisplayMetrics
import android.view.MotionEvent
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.util.unpackFloat2
import arrow.core.None
import arrow.core.some
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.arbitraries.Arbitraries.arbKeyboardAction
import inc.flide.vim8.arbitraries.Arbitraries.arbPoint
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.ime.input.InputEventDispatcher
import inc.flide.vim8.ime.input.InputFeedbackController
import inc.flide.vim8.ime.keyboard.xpad.gestures.GlideGesture
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.theme.ThemeManager
import inc.flide.vim8.ime.theme.blendARGB
import inc.flide.vim8.keyboardManager
import inc.flide.vim8.themeManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.OfTypeMatcher
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verifyOrder
import io.mockk.verifySequence

class KeyboardControllerSpec : FunSpec({
    lateinit var context: Context
    lateinit var event: MotionEvent
    lateinit var xpadKeyboard: Keyboard
    lateinit var eventDispatcher: InputEventDispatcher
    lateinit var keyboardCircle: Keyboard.Circle
    lateinit var isDynamicCircleOverlayEnabled: PreferenceData<Boolean>
    lateinit var showTrail: PreferenceData<Boolean>
    val colorScheme = lightColorScheme()

    beforeSpec {
        mockkConstructor(GlideGesture.Detector::class)
        mockkStatic(::appPreferenceModel)
        mockkStatic(Context::keyboardManager)
        mockkStatic(Context::themeManager)
        mockkObject(Vim8ImeService)

        context = mockk<Context> {
            every { resources } returns mockk {
                val dm = DisplayMetrics()
                dm.density = 1f
                every { displayMetrics } returns dm
            }
            every { keyboardManager() } returns lazy {
                mockk {
                    every { inputEventDispatcher } answers { eventDispatcher }
                }
            }
            every { themeManager() } returns lazy {
                mockk {
                    every { currentTheme } returns mockk {
                        every { value } returns ThemeManager.ThemeInfo(
                            colorScheme,
                            ThemeManager.FixedTrailColor(Color.Black)
                        )
                    }
                }
            }
        }

        val inputFeedbackController = mockk<InputFeedbackController>(relaxed = true)
        every { Vim8ImeService.inputFeedbackController() } returns inputFeedbackController

        every { appPreferenceModel() } returns CachedPreferenceModel(
            mockk<AppPrefs> {
                every { keyboard } returns mockk {
                    every { trail } returns mockk {
                        every { isVisible } answers { showTrail }
                    }
                    every { circle } returns mockk {
                        every { dynamic } returns mockk {
                            every { isOverlayEnabled } answers { isDynamicCircleOverlayEnabled }
                        }
                    }
                }
            }
        )
    }

    beforeTest {
        event = mockk {
            every { x } returns 0f
            every { y } returns 0f
        }
        xpadKeyboard = mockk(relaxed = true) {
            every { circle } answers { keyboardCircle }
            every { trailColor } returns Color.Black
        }
        isDynamicCircleOverlayEnabled = mockk<PreferenceData<Boolean>>()
        showTrail = mockk<PreferenceData<Boolean>>()
        keyboardCircle = mockk<Keyboard.Circle>(relaxed = true)
        eventDispatcher = mockk<InputEventDispatcher>(relaxed = true)
    }

    context("drawSectors") {
        withData(nameFn = { "Has dynamic circle $it" }, listOf(false, true)) { hasDynamicCircle ->
            withData(nameFn = { "Has overlay $it" }, listOf(false, true)) { hasOverlay ->
                val drawScope = mockk<DrawScope>(relaxed = true)
                every { keyboardCircle.hasVirtualCentre } returns hasDynamicCircle
                every { isDynamicCircleOverlayEnabled.get() } returns hasOverlay
                val controller = KeyboardController(context).also { it.keyboard = xpadKeyboard }
                controller.drawSectors(drawScope, Color.Black)
                verifySequence {
                    drawScope.drawCircle(
                        Color.Black,
                        any<Float>(),
                        any<Offset>(),
                        style = any<DrawStyle>()
                    )
                    drawScope.drawPath(any<Path>(), Color.Black, style = any<DrawStyle>())
                    if (hasDynamicCircle && hasOverlay) {
                        drawScope.drawCircle(
                            Color.Black.blendARGB(colorScheme.background, 0.65f)
                                .copy(alpha = 0.75f),
                            any<Float>(),
                            any<Offset>(),
                            style = any<DrawStyle>()
                        )
                    }
                }
            }
        }
    }

    test("drawTrail") {
        val points = Arb.list(arbPoint, 1..10).next()
        val drawScope = mockk<DrawScope>(relaxed = true)
        val controller = KeyboardController(context).also { it.keyboard = xpadKeyboard }
        controller.drawTrail(drawScope, points)
        verifySequence {
            for (point in points) {
                drawScope.drawCircle(
                    Color.Black,
                    point.radius,
                    point.center
                )
            }
        }
    }

    context("onTouchEventInternal has trail") {
        withData(nameFn = { "showTrail $it" }, listOf(false, true)) { hasShowTrail ->
            withData(nameFn = { "glide detector $it" }, listOf(false, true)) { glide ->
                every { showTrail.get() } returns hasShowTrail
                every {
                    constructedWith<GlideGesture.Detector>(
                        OfTypeMatcher<GlideGesture.Listener>(
                            GlideGesture.Listener::class
                        )
                    ).onTouchEvent(any())
                } returns glide
                every { event.actionMasked } returns MotionEvent.ACTION_DOWN
                val controller = KeyboardController(context).also { it.keyboard = xpadKeyboard }
                controller.onTouchEventInternal(event)
                controller.hasTrail shouldBe (hasShowTrail && glide)
            }
        }
    }

    context("onTouchEventInternal") {
        test("keyPress") {
            every { showTrail.get() } returns false
            val movements = listOf(
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.INSIDE_CIRCLE
                ) to arbKeyboardAction.next(),
                listOf(
                    FingerPosition.INSIDE_CIRCLE,
                    FingerPosition.BOTTOM,
                    FingerPosition.LEFT,
                    FingerPosition.TOP,
                    FingerPosition.INSIDE_CIRCLE
                ) to arbKeyboardAction.next()
            )
            every { xpadKeyboard.hasAction(any()) } returns false
            every { xpadKeyboard.action(any(), any()) } returns None
            for ((movement, action) in movements) {
                every { xpadKeyboard.hasAction(movement) } returns true
                every { xpadKeyboard.action(movement, any()) } returns action.some()
            }

            every { event.actionMasked } answers { MotionEvent.ACTION_DOWN } andThenAnswer {
                MotionEvent.ACTION_MOVE
            }

            every { keyboardCircle.isPointInsideCircle(any()) } answers {
                unpackFloat2(firstArg<Long>()).toInt() == FingerPosition.INSIDE_CIRCLE.ordinal
            }

            every { keyboardCircle.getSectorOfPoint(any()) } answers {
                FingerPosition.entries[unpackFloat2(firstArg<Long>()).toInt()]
            }

            val sequence = movements
                .fold(emptyList<FingerPosition>()) { acc, current ->
                    if (acc.isEmpty()) {
                        current.first
                    } else {
                        if (acc.last() == FingerPosition.INSIDE_CIRCLE &&
                            current.first.first() == FingerPosition.INSIDE_CIRCLE
                        ) {
                            acc + current.first.subList(1, current.first.size)
                        } else {
                            acc + current.first
                        }
                    }
                }

            every { event.y } returnsMany sequence.map {
                it.ordinal.toFloat()
            }

            val controller = KeyboardController(context).also { it.keyboard = xpadKeyboard }

            sequence.forEach { _ -> controller.onTouchEventInternal(event) }

            verifyOrder {
                for ((_, action) in movements) {
                    eventDispatcher.sendDownUp(action, false)
                }
            }
        }
    }

    afterSpec {
        clearStaticMockk(Context::class)
        clearConstructorMockk(GlideGesture.Detector::class)
    }
})
