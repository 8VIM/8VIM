package inc.flide.vim8.models

import android.content.Context
import android.net.Uri
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries.arbEmbeddedLayout
import inc.flide.vim8.arbitraries.Arbitraries.arbKeyboardData
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import java.util.TreeMap

class AvailableLayoutsSpec : WordSpec({
    val prefs = mockk<AppPrefs>()
    val layoutPref = mockk<AppPrefs.Layout>()
    val customPref = mockk<AppPrefs.Layout.Custom>()
    val currentLayout = mockk<PreferenceData<Layout<*>>>()
    val history = mockk<PreferenceData<Set<String>>>()

    val context = mockk<Context>()

    val embeddedLayouts = TreeMap(
        Arb
            .map(Arb.string(10, 20), arbEmbeddedLayout, 2, 10).next()
    )

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        mockkStatic(::embeddedLayouts)
        mockkStatic(Layout<*>::loadKeyboardData)
        mockkStatic(Uri::parse)

        embeddedLayouts.forEach { (_, layout) ->
            every { layout.loadKeyboardData(any()) } returns arbKeyboardData.next().right()
        }

        every { Uri.parse(any()) } answers { mockk() }

        every { prefs.layout } returns layoutPref
        every { layoutPref.current } returns currentLayout
        every { layoutPref.custom } returns customPref
        every { customPref.history } returns history
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
        every { embeddedLayouts(any()) } returns embeddedLayouts
    }

    beforeTest {
        mockkObject(AvailableLayouts)
        every { AvailableLayouts.instance } answers { AvailableLayouts(context) }
        every { currentLayout.default } returns embeddedLayouts.values.first()
        every { currentLayout.get() } returns embeddedLayouts.values.first()
        justRun { history.observe(any()) }
        every { history.get() } returns emptySet()
    }

    "Loading layouts" When {
        "find the index of a previous config" should {
            "get the right index" {
                every { currentLayout.get() } returns embeddedLayouts.values.toList()[1]!!
                val availableLayouts = AvailableLayouts.instance
                availableLayouts.index shouldBe 1
            }
        }

        "custom layout history is empty" should {
            "get only embedded layouts" {
                val availableLayouts = AvailableLayouts.instance
                availableLayouts.displayNames shouldContainExactly embeddedLayouts.keys
            }
        }
    }

    afterTest {
        clearMocks(currentLayout, history)
    }
})
