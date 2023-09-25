package inc.flide.vim8.ime

import android.content.Context
import arrow.core.left
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries.arbEmbeddedLayout
import inc.flide.vim8.arbitraries.Arbitraries.arbKeyboardData
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.ime.actionlisteners.MainKeypadActionListener
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.CustomLayout
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.models.Layout
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.models.embeddedLayouts
import inc.flide.vim8.models.error.ExceptionWrapperError
import inc.flide.vim8.models.info
import inc.flide.vim8.models.loadKeyboardData
import inc.flide.vim8.models.toCustomLayout
import inc.flide.vim8.models.yaml.name
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.string
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.random.Random

class AvailableLayoutsSpec : WordSpec({
    val prefs = mockk<AppPrefs>()
    val layoutPref = mockk<AppPrefs.Layout>(relaxed = true)
    val customPref = mockk<AppPrefs.Layout.Custom>(relaxed = true)
    val currentLayout = mockk<PreferenceData<Layout<*>>>(relaxed = true)
    val history = mockk<PreferenceData<Set<String>>>(relaxed = true)
    val customLayout = mockkClass(CustomLayout::class)

    val context = mockk<Context>()

    val embeddedLayouts = Arb.list(
        Arb.pair(
            arbEmbeddedLayout,
            arbKeyboardData.flatMap { keyboardData ->
                Arb.string(10, 20).map { KeyboardData.info.name.set(keyboardData, it) }
            }
        ),
        2..10
    ).next()

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        mockkStatic(::embeddedLayouts)
        mockkStatic(String::toCustomLayout)
        mockkObject(MainKeypadActionListener)
        justRun { MainKeypadActionListener.rebuildKeyboardData(any()) }

        embeddedLayouts.forEach { (layout, keyboardData) ->
            every { layout.loadKeyboardData(any()) } returns keyboardData.right()
        }

        every { prefs.layout } returns layoutPref
        every { layoutPref.current } returns currentLayout
        every { layoutPref.custom } returns customPref
        every { customPref.history } returns history
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
        every { embeddedLayouts(any()) } returns embeddedLayouts
    }

    beforeTest {
        mockkObject(AvailableLayouts)
        every { AvailableLayouts.instance } answers {
            AvailableLayouts(context)
        }
        every { currentLayout.default } returns embeddedLayouts.first().first
        every { currentLayout.get() } returns embeddedLayouts.first().first
        every { history.get() } returns emptySet()
    }

    "Loading layouts" When {
        "find the index of a previous config" should {
            "get the right index" {
                every { currentLayout.get() } returns embeddedLayouts[1].first
                val availableLayouts = AvailableLayouts.instance
                availableLayouts.index shouldBe 1
            }
        }

        "custom layout history" should {
            "get only embedded layouts if the history is empty" {
                val availableLayouts = AvailableLayouts.instance
                val expected = embeddedLayouts.map { it.second.toString() }
                availableLayouts.displayNames shouldContainExactly expected
            }

            "get only embedded layouts if the history is not empty" {
                val uri = "uri"
                val keyboardData = arbKeyboardData.next()
                every { uri.toCustomLayout() } returns customLayout
                every { currentLayout.get() } returns customLayout
                every { customLayout.loadKeyboardData(any()) } returns keyboardData.right()
                every { history.get() } returns setOf(uri)
                val availableLayouts = AvailableLayouts.instance
                val strings = embeddedLayouts.map { it.second.toString() } + keyboardData.toString()
                availableLayouts.displayNames shouldContainExactly strings
                availableLayouts.index shouldBe embeddedLayouts.size
            }

            "fallback to default" {
                val uri = "uri"
                every { uri.toCustomLayout() } returns customLayout
                every { currentLayout.get() } returns customLayout
                every { customLayout.loadKeyboardData(any()) } returns ExceptionWrapperError(
                    Exception()
                ).left()
                every { history.get() } returns setOf(uri)
                justRun { history.set(any()) }
                val availableLayouts = AvailableLayouts.instance
                val expected = embeddedLayouts.map { it.second.toString() }
                availableLayouts.displayNames shouldContainExactly expected
                availableLayouts.index shouldBe 0
            }
        }
    }

    "Select a layout" should {
        "which is an embedded layout" {
            val layouts = embeddedLayouts.map { it.first }
            val index = Random.nextInt(1, layouts.size)
            val availableLayouts = AvailableLayouts.instance
            availableLayouts.selectLayout(index)
            verify { MainKeypadActionListener.rebuildKeyboardData(any()) }
            verify { currentLayout.set(layouts[index]) }
            availableLayouts.index shouldBe index
        }

        "which is a custom layout" {
            val uri = "uri"
            every { uri.toCustomLayout() } returns customLayout
            every { customLayout.loadKeyboardData(any()) } returns arbKeyboardData.next().right()
            every { history.get() } returns setOf(uri)
            val index = embeddedLayouts.size
            val availableLayouts = AvailableLayouts.instance
            availableLayouts.selectLayout(index)
            verify { MainKeypadActionListener.rebuildKeyboardData(any()) }
            verify { currentLayout.set(customLayout) }
            availableLayouts.index shouldBe index
        }

        "which is not a valid index" {
            val index = embeddedLayouts.size
            val availableLayouts = AvailableLayouts.instance
            availableLayouts.selectLayout(index)
            availableLayouts.index shouldBe 0
        }
    }

    afterTest {
        clearMocks(currentLayout, history, currentLayout)
    }
})
