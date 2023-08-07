package inc.flide.vim8.datastore.model

import android.content.SharedPreferences
import inc.flide.vim8.lib.android.tryOrNull

interface PreferenceSerDe<V : Any> {
    fun serialize(editor: SharedPreferences.Editor, key: String, value: V)
    fun deserialize(sharedPreferences: SharedPreferences, key: String, default: V): V
    fun deserialize(value: Any?): V?
}

object BooleanPreferenceSerde : PreferenceSerDe<Boolean> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Boolean
    ): Boolean {
        return tryOrNull {
            sharedPreferences.getBoolean(key, default)
        } ?: default
    }

    override fun deserialize(value: Any?): Boolean? {
        return tryOrNull { value as Boolean? }
    }
}

object FloatPreferenceSerde : PreferenceSerDe<Float> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: Float) {
        editor.putFloat(key, value)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Float
    ): Float {
        return tryOrNull {
            sharedPreferences.getFloat(key, default)
        } ?: default
    }

    override fun deserialize(value: Any?): Float? {
        return tryOrNull { value as Float? }
    }
}

object IntPreferenceSerde : PreferenceSerDe<Int> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: Int) {
        editor.putInt(key, value)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Int
    ): Int {
        return tryOrNull {
            sharedPreferences.getInt(key, default)
        } ?: default
    }

    override fun deserialize(value: Any?): Int? {
        return tryOrNull { value as Int? }
    }
}

object StringPreferenceSerde : PreferenceSerDe<String> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: String) {
        editor.putString(key, value)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: String
    ): String {
        return tryOrNull {
            sharedPreferences.getString(key, default)
        } ?: default
    }

    override fun deserialize(value: Any?): String? {
        return tryOrNull { value as String? }
    }
}

object StringSetPreferenceSerde : PreferenceSerDe<Set<String>> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: Set<String>) {
        editor.putStringSet(key, value)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Set<String>
    ): Set<String> {
        return tryOrNull {
            sharedPreferences.getStringSet(key, default)
        } ?: default
    }

    @Suppress("unchecked_cast")
    override fun deserialize(value: Any?): Set<String>? {
        return tryOrNull { value as Set<String>? }
    }
}
