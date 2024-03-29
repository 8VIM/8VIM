package inc.flide.vim8.ime.layout

import android.content.Context
import arrow.core.none
import arrow.core.some
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.parsers.CacheParser
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.boolean
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.File

class CacheSpec : FunSpec({
    lateinit var context: Context
    lateinit var cacheParser: CacheParser
    lateinit var cachePref: PreferenceData<Set<String>>

    beforeSpec {
        mockkStatic(::appPreferenceModel)

        context = mockk {
            every { cacheDir } returns File("cacheDir")
        }

        every { appPreferenceModel() } returns CachedPreferenceModel(
            mockk {
                every { layout } returns mockk {
                    every { cache } answers { cachePref }
                }
            }
        )
    }

    beforeTest {
        cacheParser = mockk()
        cachePref = mockk(relaxed = true) {
            every { get() } returns setOf("test")
        }
    }

    context("Load") {
        withData(nameFn = { "Exists in cache: $it" }, Exhaustive.boolean().values) { exists ->
            withData(nameFn = { "Error loading: $it" }, Exhaustive.boolean().values) { error ->
                val name = if (exists) "test" else "not_in_cache"
                val keyboardData = mockk<KeyboardData>()

                every { cacheParser.load(any()) } returns if (error) none() else keyboardData.some()

                val cache = Cache(cacheParser, context)
                val result = cache.load(name)
                if (exists && !error) {
                    result shouldBeSome keyboardData
                    if (error) {
                        verify { cachePref.set(emptySet()) }
                    }
                } else {
                    result.shouldBeNone()
                }
            }
        }
    }

    context("Save") {
        withData(nameFn = { "Exists in cache: $it" }, Exhaustive.boolean().values) { exists ->
            withData(nameFn = { "Saved: $it" }, Exhaustive.boolean().values) { success ->
                every { cachePref.get() } returns if (exists) setOf("test") else emptySet()

                every { cacheParser.save(any(), any()) } returns success
                Cache(cacheParser, context).add("test", mockk())
                if (!exists && success) {
                    verify { cachePref.set(setOf("test"), true) }
                }
            }
        }
    }
})
