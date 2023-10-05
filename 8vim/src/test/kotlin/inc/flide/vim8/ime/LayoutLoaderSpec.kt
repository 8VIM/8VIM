package inc.flide.vim8.ime

import android.content.res.Resources
import arrow.core.None
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries
import inc.flide.vim8.ime.layout.Cache
import inc.flide.vim8.ime.layout.models.CharacterSet
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.yaml.LayoutInfo
import inc.flide.vim8.ime.parsers.Yaml
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import java.io.InputStream

class LayoutLoaderSpec : FunSpec({
    val resources = mockk<Resources>()
    val inputStream = mockk<InputStream>(relaxed = true)

    beforeSpec {
        mockkStatic(Yaml::class)
        mockkObject(Cache)
        val cache = mockk<Cache>(relaxed = true)
        every { Cache.instance } returns cache
        every { cache.load(any()) } returns None
    }

    beforeTest {
        every { resources.openRawResource(any()) } returns inputStream
    }

    afterTest {
        clearMocks(resources)
        clearStaticMockk(Yaml::class, Cache::class)
        LayoutLoader.layoutIndependentKeyboardData = null
    }

    context("Loading keyboardData") {
        test("without a previous") {
            val action = Arbitraries.arbKeyboardAction.next()
            val first = listOf(FingerPosition.INSIDE_CIRCLE) to action
            val second = listOf(FingerPosition.TOP) to action
            every { Yaml.readKeyboardData(any()) } returnsMany listOf(
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
