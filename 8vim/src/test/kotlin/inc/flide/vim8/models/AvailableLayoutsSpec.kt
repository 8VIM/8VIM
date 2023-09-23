package inc.flide.vim8.models

import android.content.Context
import android.net.Uri
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries.arbEmbeddedLayout
import inc.flide.vim8.arbitraries.Arbitraries.arbKeyboardData
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.lib.android.ext.CustomLayoutHistoryManager
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
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic

class AvailableLayoutsSpec : WordSpec({
    val prefs = mockk<AppPrefs>()
    val layoutPref = mockk<AppPrefs.Layout>()
    val customPref = mockk<AppPrefs.Layout.Custom>()
    val currentLayout = mockk<PreferenceData<Layout<*>>>()
    val history = mockk<PreferenceData<Set<String>>>(relaxed = true)

    val context = mockk<Context>()
    val customLayoutHistoryManager = mockk<CustomLayoutHistoryManager>(relaxed = true)
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
        mockkStatic(Layout<*>::loadKeyboardData)
        mockkStatic(Uri::parse)

        embeddedLayouts.forEach { (layout, keyboardData) ->
            every { layout.loadKeyboardData(any()) } returns keyboardData.right()
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
        every { AvailableLayouts.instance } answers {
            AvailableLayouts(
                context,
                customLayoutHistoryManager
            )
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

        "custom layout history is empty" should {
            "get only embedded layouts" {
                val availableLayouts = AvailableLayouts.instance
                val expected = embeddedLayouts.map { it.second.toString() }
                availableLayouts.displayNames shouldContainExactly expected
            }
        }
    }

    afterTest {
        clearMocks(currentLayout, history)
    }
})
