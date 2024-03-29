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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class CircleSpec : FunSpec() {
    init {
        lateinit var isDynamicCircleEnabled: PreferenceData<Boolean>
        val keyboardCircle = Keyboard.Circle(radius = 10f)

        beforeSpec {
            mockkStatic(::appPreferenceModel)
            every { appPreferenceModel() } returns CachedPreferenceModel(
                mockk<AppPrefs> {
                    every { keyboard } returns mockk {
                        every { circle } returns mockk {
                            every { dynamic } returns mockk {
                                every { isEnabled } answers { isDynamicCircleEnabled }
                            }
                        }
                    }
                }
            )
        }

        beforeTest {
            isDynamicCircleEnabled = mockk<PreferenceData<Boolean>>(relaxed = true)
        }

        context("is point inside circle") {
            withData(
                (Offset(1f, 1f) to true),
                (Offset(20f, 20f) to false)
            ) { (point, expected) ->
                keyboardCircle.isPointInsideCircle(point) shouldBe expected
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
                keyboardCircle.getSectorOfPoint(point) shouldBe position
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
                    point = Offset(2f, 0f),
                    expected = Offset(1.0f, 0f)
                )
            ) { (isDynamic, point, expected) ->
                val circleWithCentre = Keyboard.Circle(centre, 10f)
                every { isDynamicCircleEnabled.get() } returns isDynamic
                circleWithCentre.initVirtual(point)
                val expectedCentre = centre + expected
                val expectedHasCentre = expected != Offset.Zero
                circleWithCentre.hasVirtualCentre shouldBe expectedHasCentre
                circleWithCentre.virtualCentre shouldBe expectedCentre
            }
        }
    }

    private data class VirtualCentreTestParam(
        val isDynamic: Boolean = true,
        val point: Offset,
        val expected: Offset
    )
}
