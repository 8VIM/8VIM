package inc.flide.vim8.ime

import android.content.Context
import android.content.res.Resources
import arrow.core.None
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries
import inc.flide.vim8.ime.layout.Cache
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.yaml.LayoutInfo
import inc.flide.vim8.ime.layout.parsers.LayoutParser
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.io.InputStream

class LayoutLoaderSpec : FunSpec({
    val context = mockk<Context>()
    val resources = mockk<Resources>()
    val inputStream = mockk<InputStream>(relaxed = true)
    val cache = mockk<Cache>(relaxed = true)
    val layoutParser = mockk<LayoutParser>(relaxed = true)

    beforeSpec {
        every { cache.load(any()) } returns None
        every { context.resources } returns resources
    }

    beforeTest {
        every { resources.openRawResource(any()) } returns inputStream
    }

    afterTest {
        clearMocks(resources, layoutParser)
    }

    context("Loading keyboardData") {
        test("without a previous") {
            val action = Arbitraries.arbKeyboardAction.next()
            val first = listOf(FingerPosition.INSIDE_CIRCLE) to action
            val second = listOf(FingerPosition.TOP) to action
            every { layoutParser.readKeyboardData(any()) } returnsMany listOf(
                KeyboardData(
                    actionMap = mapOf(first),
                    characterSets = emptyList()
                ).right(),
                KeyboardData(
                    actionMap = mapOf(first),
                    characterSets = emptyList()
                ).right(),
                KeyboardData(actionMap = mapOf(second)).right(),
                KeyboardData(info = LayoutInfo(name = "test")).right()
            )
            YamlLayoutLoader(layoutParser, cache, context)
                .loadKeyboardData(mockk()) shouldBeRight KeyboardData(
                actionMap = mapOf(first, second),
                info = LayoutInfo(name = "test")
            )
        }
    }
})
