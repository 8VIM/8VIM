package inc.flide.vim8.ime.layout

import android.content.Context
import arrow.core.None
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.yaml.versions.common.LayoutInfo
import inc.flide.vim8.ime.layout.parsers.LayoutParser
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import java.io.InputStream

class LayoutLoaderSpec : FunSpec({
    lateinit var context: Context
    lateinit var cache: Cache
    lateinit var layoutParser: LayoutParser

    beforeSpec {
        context = mockk {
            every { resources } returns mockk {
                every { openRawResource(any()) } returns mockk<InputStream>(relaxed = true)
            }
        }
        cache = mockk(relaxed = true) {
            every { load(any()) } returns None
        }
    }

    beforeTest {
        layoutParser = mockk<LayoutParser>(relaxed = true)
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
