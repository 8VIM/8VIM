package inc.flide.vim8.datastore.nodel

import android.content.SharedPreferences
import inc.flide.vim8.datastore.model.BooleanPreferenceSerde
import inc.flide.vim8.datastore.model.FloatPreferenceSerde
import inc.flide.vim8.datastore.model.IntPreferenceSerde
import inc.flide.vim8.datastore.model.PreferenceSerDe
import inc.flide.vim8.datastore.model.StringPreferenceSerde
import inc.flide.vim8.datastore.model.StringSetPreferenceSerde
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class PreferenceSerDeSpec : FunSpec({
    val data = mapOf(
        (BooleanPreferenceSerde to ((true to false))),
        (FloatPreferenceSerde to (1f to 0f)),
        (IntPreferenceSerde to (1 to 0)),
        (StringPreferenceSerde to ("value" to "")),
        (StringSetPreferenceSerde to (setOf("value") to emptySet<String>()))
    )
    val key = "key"

    context("Serializing") {
        val editor = mockk<SharedPreferences.Editor>()
        var result: Any? = null

        @Suppress("unchecked_cast")
        fun <V : Any> PreferenceSerDe<V>.testSerialize(
            editor: SharedPreferences.Editor,
            key: String,
            value: Any
        ) {
            serialize(editor, key, value as V)
        }
        beforeTest {
            every { editor.putBoolean(key, any()) } answers {
                result = it.invocation.args[1]
                editor
            }

            every { editor.putInt(key, any()) } answers {
                result = it.invocation.args[1]
                editor
            }

            every { editor.putFloat(key, any()) } answers {
                result = it.invocation.args[1]
                editor
            }

            every { editor.putString(key, any()) } answers {
                result = it.invocation.args[1]
                editor
            }

            every { editor.putStringSet(key, any()) } answers {
                result = it.invocation.args[1]
                editor
            }
        }

        afterTest {
            clearMocks(editor)
        }

        withData(
            nameFn = { it.first::class.simpleName.orEmpty() },
            data.map { (it.key to it.value.first) }
        ) { (serde, value) ->
            serde.testSerialize(editor, key, value)
            result shouldBe value
        }
    }

    context("Deserializing from SharedPreference") {
        val wrongValue = "wrong"
        val sharedPreference = mockk<SharedPreferences>()

        @Suppress("unchecked_cast")
        fun <V : Any> PreferenceSerDe<V>.testDeserialize(
            sharedPreferences: SharedPreferences,
            key: String,
            default: Any
        ): V {
            return deserialize(sharedPreferences, key, default as V)
        }
        beforeTest {
            every {
                sharedPreference.getBoolean(
                    key,
                    any()
                )
            } returns data[BooleanPreferenceSerde]?.first as Boolean
            every { sharedPreference.getBoolean(wrongValue, any()) } throws Exception("error")

            every {
                sharedPreference.getInt(
                    key,
                    any()
                )
            } returns data[IntPreferenceSerde]?.first as Int
            every { sharedPreference.getInt(wrongValue, any()) } throws Exception("error")

            every {
                sharedPreference.getFloat(
                    key,
                    any()
                )
            } returns data[FloatPreferenceSerde]?.first as Float
            every { sharedPreference.getFloat(wrongValue, any()) } throws Exception("error")

            every {
                sharedPreference.getString(
                    key,
                    any()
                )
            } returns data[StringPreferenceSerde]?.first as String
            every { sharedPreference.getString(wrongValue, any()) } throws Exception("error")

            @Suppress("unchecked_cast")
            every {
                sharedPreference.getStringSet(
                    key,
                    any()
                )
            } returns data[StringSetPreferenceSerde]?.first as Set<String>
            every { sharedPreference.getStringSet(wrongValue, any()) } throws Exception("error")
        }

        afterTest {
            clearMocks(sharedPreference)
        }

        withData(
            nameFn = { it.first::class.simpleName.orEmpty() },
            data.map { Triple(it.key, it.value.first, it.value.second) }
        ) { (serde, value, default) ->
            serde.testDeserialize(sharedPreference, key, default) shouldBe value
            serde.testDeserialize(sharedPreference, wrongValue, default) shouldBe default
        }
    }

    context("Deserializing value") {
        withData(
            nameFn = { "${it.first::class.simpleName} -> ${it.second}" },
            (BooleanPreferenceSerde to true),
            (FloatPreferenceSerde to 1f),
            (IntPreferenceSerde to 1),
            (StringPreferenceSerde to "value"),
            (StringSetPreferenceSerde to setOf("value"))
        ) { (serde, value) ->
            serde.deserialize(null).shouldBeNull()
            serde.deserialize(Exception("wrong type")).shouldBeNull()
            serde.deserialize(value) shouldBe value
        }
    }
})
