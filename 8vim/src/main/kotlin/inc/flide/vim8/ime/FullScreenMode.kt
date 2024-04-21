package inc.flide.vim8.ime

import android.content.SharedPreferences
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import arrow.core.Option
import arrow.core.raise.option
import inc.flide.vim8.datastore.model.PreferenceSerDe
import inc.flide.vim8.lib.android.tryOrNull

interface FullScreenMode {
    fun rect(): Rect
    fun offset(): Offset
    fun isEmpty(): Boolean
    fun update(callback: (Rect) -> Rect): FullScreenMode = this
}

object EmbeddedMode : FullScreenMode {
    override fun rect(): Rect = Rect.Zero
    override fun offset(): Offset = Offset.Zero
    override fun isEmpty(): Boolean = true
    override fun toString(): String = "Embedded"
}

data class PopupMode(private val rect: Rect = Rect.Zero) : FullScreenMode {
    private val offset = Offset(rect.left, rect.top)
    override fun isEmpty(): Boolean = false
    override fun rect(): Rect = rect
    override fun offset(): Offset = offset
    override fun update(callback: (Rect) -> Rect): PopupMode = copy(rect = callback(rect))
    override fun toString(): String = "Popup: $rect"
}

object FullScreenModeSerde : PreferenceSerDe<FullScreenMode> {
    override fun serialize(
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor,
        key: String,
        value: FullScreenMode
    ) {
        val rect = value.rect()
        val previous = sharedPreferences.getString(key, "")!!.split(';')
        val encodedValue = when (value) {
            is EmbeddedMode -> listOf("embedded") + previous.drop(1)
            is PopupMode -> {
                if (rect.isEmpty) {
                    listOf("popup") + previous.drop(1)
                } else {
                    listOf("popup", rect.left, rect.top, rect.right, rect.bottom)
                }
            }

            else -> previous
        }
        editor.putString(key, encodedValue.joinToString(";"))
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: FullScreenMode
    ): FullScreenMode {
        return tryOrNull { sharedPreferences.getString(key, null) }
            ?.let { deserialize(it) } ?: default
    }

    override fun deserialize(value: Any?): FullScreenMode? = option {
        val input = Option.fromNullable(value).bind()
        val split = input.toString().split(';')
        when (split.first()) {
            "popup" -> {
                if (split.drop(1).isEmpty()) {
                    PopupMode()
                } else {
                    val (left, top, right, bottom) = split.drop(1).map { it.toFloat() }
                    PopupMode(Rect(left, top, right, bottom))
                }
            }

            else -> EmbeddedMode
        }
    }.getOrNull()
}
