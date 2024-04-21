package inc.flide.vim8.ime.ui

import android.content.SharedPreferences
import androidx.compose.ui.geometry.Rect
import arrow.core.Option
import arrow.core.raise.option
import inc.flide.vim8.datastore.model.PreferenceSerDe
import inc.flide.vim8.lib.android.tryOrNull

enum class KeyboardLayoutMode {
    EMBEDDED,
    FLOATING
}

object RectSerDe : PreferenceSerDe<Rect> {
    override fun serialize(editor: SharedPreferences.Editor, key: String, value: Rect) {
        editor.putString(
            key,
            listOf(value.left, value.top, value.right, value.bottom).joinToString(";")
        )
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Rect
    ): Rect {
        return tryOrNull { sharedPreferences.getString(key, null) }
            ?.let { deserialize(it) } ?: default
    }

    override fun deserialize(value: Any?): Rect? = option {
        val input = Option.fromNullable(value).bind()
        val (left, top, right, bottom) = input.toString().split(';').map { it.toFloat() }
        Rect(left, top, right, bottom)
    }.getOrNull()
}
