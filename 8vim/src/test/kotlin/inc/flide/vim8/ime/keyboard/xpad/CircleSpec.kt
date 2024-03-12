package inc.flide.vim8.ime.keyboard.xpad

import androidx.compose.ui.geometry.Offset
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.ime.layout.models.FingerPosition
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class CircleSpec : FunSpec({
    val dynamicCentre = mockk<PreferenceData<Boolean>>(relaxed = true)
    val circle = Keyboard.Circle(radius = 10f)

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        val prefs = mockk<AppPrefs>()
        val keyboardPref = mockk<AppPrefs.Keyboard>(relaxed = true)
        val circlePref = mockk<AppPrefs.Keyboard.Circle>(relaxed = true)

        every { prefs.keyboard } returns keyboardPref
        every { keyboardPref.circle } returns circlePref
        every { circlePref.dynamicCentre } returns dynamicCentre
        every { circlePref.dynamicCentreOffsetRatio } returns 0.25f
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
    }

    beforeTest {
        clearMocks(dynamicCentre)
    }

    context("is point inside circle") {
        withData(
            (Offset(1f, 1f) to true),
            (Offset(20f, 20f) to false)
        ) { (point, expected) ->
            circle.isPointInsideCircle(point) shouldBe expected
        }
    }

    context("get sector from a point") {
        withData(
            nameFn = {
                "Finger at (${it.second.x}, ${it.second.y})" +
                    "should be the ${it.first} sector"
            },
            (FingerPosition.TOP to Offset(0f, -10f)),
            (FingerPosition.LEFT to Offset(-10f, 0f)),
            (FingerPosition.BOTTOM to Offset(0f, 10f)),
            (FingerPosition.RIGHT to Offset(10f, 0f))
        ) { (position, point) ->
            circle.getSectorOfPoint(point) shouldBe position
        }
    }

    context("Virtual centre") {
        val centre = Offset(1f, 0f)
        withData(
            nameFn = {
                "Dynamic centre ${it.isDynamic} at ${it.point}" +
                    " should have centre ${centre + it.expected}"
            },
            VirtualCentreTestParam(
                isDynamic = false,
                point = Offset.Zero,
                expected = Offset.Zero
            ),
            VirtualCentreTestParam(
                shouldInit = false,
                isDynamic = false,
                point = Offset.Zero,
                expected = Offset.Zero
            ),
            VirtualCentreTestParam(
                point = Offset(2f, 0f),
                expected = Offset(-1.0f, 0f)
            )
        ) { (shouldInit, isDynamic, point, expected) ->
            val circleWithCentre = Keyboard.Circle(centre, 10f)
            every { dynamicCentre.get() } returns isDynamic
            if (shouldInit) {
                circleWithCentre.initVirtual(point)
            } else {
                circleWithCentre.computeVirtualCentre(point)
            }
            val expectedCentre = centre + expected
            val expectedHasCentre = expected != Offset.Zero
            circleWithCentre.hasVirtualCentre shouldBe expectedHasCentre
            circleWithCentre.virtualCentre shouldBe expectedCentre
        }
    }
})

private data class VirtualCentreTestParam(
    val shouldInit: Boolean = true,
    val isDynamic: Boolean = true,
    val point: Offset,
    val expected: Offset
)
