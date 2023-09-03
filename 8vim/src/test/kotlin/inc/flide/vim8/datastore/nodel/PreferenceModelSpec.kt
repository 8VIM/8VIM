package inc.flide.vim8.datastore.nodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import inc.flide.vim8.datastore.model.PreferenceMigrationEntry
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.PreferenceObserver
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.Call
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify

enum class Enum {
    A, B
}

class TestModel : PreferenceModel(1) {
    val booleanPref = boolean(key = "boolean", default = false)
    val stringPref = string(key = "string", default = "")
    val intPref = int(key = "int", default = 0)
    val floatPref = float(key = "float", default = 0f)
    val stringSetPref = stringSet(key = "stringSet", default = emptySet())
    val enumPref = enum(key = "enum", default = Enum.A)
    override fun migrate(
        previousVersion: Int,
        entry: PreferenceMigrationEntry
    ): PreferenceMigrationEntry {
        return when (previousVersion) {
            0 -> when (entry.key) {
                "to_remove" -> entry.reset()
                "to_rename" -> entry.transform("string", entry.rawValue)
                "boolean" -> entry.transform(entry.key, (entry.rawValue as Int) == 1)
                "int" -> entry.transform(entry.key, (entry.rawValue as Int) + 1)
                else -> entry.keepAsIs()
            }

            else -> entry.keepAsIs()
        }
    }
}

class PreferenceModelSpec : WordSpec({
    val sharedPreference = mockk<SharedPreferences>(relaxed = true)
    val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    val context = mockk<Context>()
    val sharedData = hashMapOf<String, Any?>()
    lateinit var pref: TestModel

    fun storeData(call: Call): SharedPreferences.Editor {
        val params = call.invocation.args
        val key = params[0] as String
        sharedData[key] = params[1]
        return editor
    }

    fun readData(call: Call): Any {
        val params = call.invocation.args
        val key = params[0] as String
        return sharedData.getOrDefault(key, params[1])!!
    }

    beforeSpec {
        mockkStatic(PreferenceManager::getDefaultSharedPreferences)
        every { PreferenceManager.getDefaultSharedPreferences(any()) } returns sharedPreference
        every { sharedPreference.all } returns sharedData
        every { sharedPreference.getInt(any(), any()) } answers { readData(it) as Int }
        every { sharedPreference.getFloat(any(), any()) } answers { readData(it) as Float }
        every { sharedPreference.getBoolean(any(), any()) } answers { readData(it) as Boolean }
        every { sharedPreference.getString(any(), any()) } answers { readData(it) as String }
        @Suppress("unchecked_cast")
        every {
            sharedPreference.getStringSet(
                any(),
                any()
            )
        } answers { readData(it) as Set<String> }
        every { sharedPreference.edit() } returns editor
        every { editor.remove(any()) } answers {
            val key = it.invocation.args[0] as String
            sharedData.remove(key)
            editor
        }
        every { editor.putInt(any(), any()) } answers { storeData(it) }
        every { editor.putFloat(any(), any()) } answers { storeData(it) }
        every { editor.putBoolean(any(), any()) } answers { storeData(it) }
        every { editor.putString(any(), any()) } answers { storeData(it) }
        every { editor.putStringSet(any(), any()) } answers { storeData(it) }
    }

    beforeTest {
        pref = TestModel()
    }

    "Initializing" When {
        "migrating" should {
            "not migrate data from if there are no stored preferences" {
                pref.initialize(context)
                pref.isReady().shouldBeTrue()
                sharedData shouldBe mapOf(PreferenceModel.DATASTORE_VERSION to 1)
            }

            "not migrate data from if it's the latest version" {
                sharedData[PreferenceModel.DATASTORE_VERSION] = 1
                sharedData[pref.floatPref.key] = 1f
                pref.initialize(context)
                pref.isReady().shouldBeTrue()
                sharedData shouldBe mapOf(
                    "float" to 1f,
                    PreferenceModel.DATASTORE_VERSION to 1
                )
            }

            "migrate data" {
                sharedData["to_remove"] = 1
                sharedData["boolean"] = 1
                sharedData["to_rename"] = "value"
                sharedData[pref.intPref.key] = 1
                pref.initialize(context)
                pref.isReady().shouldBeTrue()
                sharedData shouldBe mapOf(
                    "string" to "value",
                    "boolean" to true,
                    "int" to 2,
                    PreferenceModel.DATASTORE_VERSION to 1
                )
            }
        }
    }

    "loading previous preferences" should {
        "get the correct  value" {
            sharedData[PreferenceModel.DATASTORE_VERSION] = 1
            sharedData["int"] = 1
            pref.initialize(context)
            pref.isReady().shouldBeTrue()
            pref.intPref.get() shouldBe 1
        }
        "fallback to default value for incorrect data" {
            sharedData[PreferenceModel.DATASTORE_VERSION] = 1
            sharedData["int"] = "string"
            pref.initialize(context)
            pref.isReady().shouldBeTrue()
            pref.intPref.get() shouldBe pref.intPref.default
        }
    }

    "Updating value" should {
        "store data" {
            sharedData[PreferenceModel.DATASTORE_VERSION] = 1
            pref.initialize(context)
            pref.isReady().shouldBeTrue()
            pref.intPref.get() shouldBe pref.intPref.default
            pref.intPref.set(1)
            sharedData shouldBe mapOf(
                "int" to 1,
                PreferenceModel.DATASTORE_VERSION to 1
            )
        }
    }

    "enum type" When {
        "loading data" should {
            "get the correct value" {
                sharedData[PreferenceModel.DATASTORE_VERSION] = 1
                sharedData["enum"] = Enum.B.toString()
                pref.initialize(context)
                pref.isReady().shouldBeTrue()
                pref.enumPref.get() shouldBe Enum.B
            }

            "fallback to default if it's not valid" {
                sharedData[PreferenceModel.DATASTORE_VERSION] = 1
                sharedData["enum"] = "string"
                pref.initialize(context)
                pref.isReady().shouldBeTrue()
                pref.enumPref.get() shouldBe pref.enumPref.default
            }
        }
    }

    "observe" When {
        "onReady" should {
            val owner = mockk<LifecycleOwner>()
            val lifecycle = mockk<Lifecycle>()

            beforeTest {
                every { owner.lifecycle } returns lifecycle
            }

            afterTest {
                clearMocks(owner, lifecycle)
            }

            "lifecycle is already destroyed" {
                val observer = mockk<PreferenceObserver<Boolean>>()
                every { observer.onChanged(any()) } just Runs
                every { lifecycle.currentState } returns Lifecycle.State.DESTROYED
                pref.onReady(owner, observer)
                pref.initialize(context)
                verify(exactly = 0) { observer.onChanged(true) }
            }

            "lifecycle changed" {
                val observer = mockk<PreferenceObserver<Boolean>>()
                val lifecycleObserver = slot<LifecycleEventObserver>()
                every { observer.onChanged(any()) } just Runs
                every { lifecycle.currentState } returnsMany listOf(
                    Lifecycle.State.CREATED,
                    Lifecycle.State.INITIALIZED
                )
                every { lifecycle.addObserver(capture(lifecycleObserver)) } just Runs
                pref.onReady(owner, observer)
                lifecycleObserver.captured.onStateChanged(owner, Lifecycle.Event.ON_CREATE)
                pref.initialize(context)
                verify { observer.onChanged(true) }
            }

            "lifecycle changed to destroyed" {
                val observer = mockk<PreferenceObserver<Boolean>>()
                val lifecycleObserver = slot<LifecycleEventObserver>()
                every { observer.onChanged(any()) } just Runs
                every { lifecycle.currentState } returnsMany listOf(
                    Lifecycle.State.CREATED,
                    Lifecycle.State.DESTROYED
                )
                every { lifecycle.addObserver(capture(lifecycleObserver)) } just Runs
                pref.onReady(owner, observer)
                lifecycleObserver.captured.onStateChanged(owner, Lifecycle.Event.ON_CREATE)
                pref.initialize(context)
                verify(exactly = 0) { observer.onChanged(true) }
            }

            "before initialize call" {
                val observer = mockk<PreferenceObserver<Boolean>>()
                every { observer.onChanged(any()) } just Runs
                every { lifecycle.currentState } returns Lifecycle.State.CREATED
                every { lifecycle.addObserver(any()) } just Runs
                pref.onReady(owner, observer)
                pref.initialize(context)
                verify { observer.onChanged(true) }
            }

            "after initialize call" {
                val observer = mockk<PreferenceObserver<Boolean>>()
                every { observer.onChanged(any()) } just Runs
                every { lifecycle.currentState } returns Lifecycle.State.CREATED
                every { lifecycle.addObserver(any()) } just Runs
                pref.initialize(context)
                pref.onReady(owner, observer)
                verify { observer.onChanged(true) }
            }
        }

        "preference" should {
            "new value" {
                val observer = mockk<PreferenceObserver<Enum>>()
                every { observer.onChanged(any()) } just Runs
                sharedData[PreferenceModel.DATASTORE_VERSION] = 1
                pref.initialize(context)
                pref.enumPref.observe(observer)
                pref.isReady().shouldBeTrue()
                pref.enumPref.set(Enum.B)
                pref.onSharedPreferenceChanged(null, "enum")
                verify { observer.onChanged(Enum.B) }
            }
        }
    }

    afterTest {
        sharedData.clear()
    }
})
