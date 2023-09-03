package inc.flide.vim8.ime

import android.content.res.Resources
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries
import inc.flide.vim8.models.CharacterSet
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.models.yaml.LayoutInfo
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.next
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.InputStream

class LayoutLoaderSpec : FunSpec({
    val resources = mockk<Resources>()
    val inputStream = mockk<InputStream>()

    beforeSpec {
        mockkStatic(KeyboardDataYamlParser::class)
        every { inputStream.close() } just Runs
    }

    beforeTest {
        every { resources.openRawResource(any()) } returns inputStream
    }

    afterTest {
        clearMocks(resources)
        clearStaticMockk(KeyboardDataYamlParser::class)
        LayoutLoader.layoutIndependentKeyboardData = null
    }

    context("Loading keyboardData") {
        test("without a previous") {
            val action = Arbitraries.arbKeyboardAction.next()
            val first = listOf(FingerPosition.INSIDE_CIRCLE) to action
            val second = listOf(FingerPosition.TOP) to action
            every { KeyboardDataYamlParser.readKeyboardData(any()) } returnsMany listOf(
                KeyboardData(
                    actionMap = mapOf(first),
                    characterSets = listOf(CharacterSet("t"))
                ).right(),
                KeyboardData(
                    actionMap = mapOf(first),
                    characterSets = listOf(CharacterSet(), CharacterSet("u", "T"))
                ).right(),
                KeyboardData(actionMap = mapOf(second)).right(),
                KeyboardData(info = LayoutInfo(name = "test")).right()
            )
            LayoutLoader.loadKeyboardData(
                resources,
                mockk()
            ) shouldBeRight KeyboardData(
                actionMap = mapOf(first, second),
                info = LayoutInfo(name = "test")
            )
        }
    }
})
