package inc.flide.vim8.ime.clipboard

import android.content.ClipData
import android.content.Context
import androidx.lifecycle.MutableLiveData
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceObserver
import inc.flide.vim8.lib.android.systemService
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic

class ClipboardManagerSpec : FunSpec({

    lateinit var context: Context
    lateinit var androidClipboardManager: android.content.ClipboardManager
    lateinit var clipboardEnabled: PreferenceData<Boolean>
    lateinit var clipboardHistory: PreferenceData<Set<String>>
    lateinit var clipboardMaxHistory: PreferenceData<Int>
    lateinit var observer: PreferenceObserver<Int>

    beforeSpec {
        mockkStatic(Context::class)
        mockkStatic(::appPreferenceModel)
        mockkConstructor(MutableLiveData::class)

        context = mockk(relaxed = true) {
            every { systemService(android.content.ClipboardManager::class) } answers {
                androidClipboardManager
            }
        }

        every { appPreferenceModel() } returns CachedPreferenceModel(
            mockk {
                every { clipboard } returns mockk {
                    every { history } answers { clipboardHistory }
                    every { maxHistory } answers { clipboardMaxHistory }
                    every { enabled } answers { clipboardEnabled }
                }
            }
        )

        var dataHistory = emptyList<String>()

        every {
            anyConstructed<MutableLiveData<List<String>>>().value = any()
        } propertyType List::class answers {
            @Suppress("Unchecked_cast")
            dataHistory = value as List<String>
        }

        every {
            anyConstructed<MutableLiveData<List<String>>>().value
        } answers {
            dataHistory
        }

        every {
            anyConstructed<MutableLiveData<List<String>>>().postValue(any())
        } answers {
            dataHistory = firstArg<List<String>>()
        }
    }

    beforeTest {
        androidClipboardManager = mockk(relaxed = true)
        clipboardHistory = mockk(relaxed = true) {
            every { get() } returns emptySet()
        }
        clipboardMaxHistory = mockk(relaxed = true) {
            every { observe(any()) } answers { observer = firstArg() }
            every { get() } returns 2
        }

        clipboardEnabled = mockk(relaxed = true)
    }

    context("Load history") {
        test("No history") {
            val manager = ClipboardManager(context)
            manager.history.value shouldBe emptyList()
        }
        test("With history") {
            val history = Arb.list(Arb.string(4..4), 2..2).next()
            every { clipboardHistory.get() } returns history.withIndex()
                .map { (time, text) -> "[$time] $text" }.toSet()
            val manager = ClipboardManager(context)
            manager.history.value shouldContainExactlyInAnyOrder history.reversed()
        }
    }

    test("New max history") {
        val history = Arb.list(Arb.string(4..4), 2..2).next()
        every { clipboardHistory.get() } returns history.withIndex()
            .map { (time, text) -> "[$time] $text" }.toSet()
        val manager = ClipboardManager(context)
        manager.history.value shouldContainExactlyInAnyOrder history.reversed()
        observer.onChanged(1)
        manager.history.value shouldContainExactlyInAnyOrder history.drop(1)
    }

    context("Add clip") {
        withData(nameFn = { "Enabled $it" }, listOf(true, false)) { isEnabled ->
            every { clipboardEnabled.get() } returns isEnabled
            val history = Arb.list(Arb.string(4..4), 3..3).next()
            every { androidClipboardManager.primaryClip } returnsMany history.map {
                mockk<ClipData> {
                    every { getItemAt(0) } returns mockk {
                        every { text } returns it
                    }
                }
            }
            val window = history.reversed().windowed(2).reversed()
            every { clipboardHistory.get() } returnsMany (
                listOf(emptyList<String>()) + window.map {
                    it.withIndex().map { (time, text) -> "[${it.size - time}] $text" }
                }
                )
                .map { it.toSet() }
            val manager = ClipboardManager(context)
            for (data in window) {
                val expected = if (!isEnabled) emptyList() else data
                manager.onPrimaryClipChanged()
                manager.history.value shouldContainExactlyInAnyOrder expected
            }
        }
    }

    afterSpec {
        clearStaticMockk(Context::class)
        clearConstructorMockk(MutableLiveData::class)
    }
})
