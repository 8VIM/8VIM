package inc.flide.vim8.models

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import arrow.core.Either
import arrow.core.None
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.right
import arrow.core.some
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.ListPreferenceEntry
import inc.flide.vim8.datastore.ui.listPrefEntries
import inc.flide.vim8.ime.InputMethodServiceHelper
import inc.flide.vim8.models.error.ExceptionWrapperError
import inc.flide.vim8.models.error.LayoutError
import inc.flide.vim8.structures.info
import inc.flide.vim8.structures.yaml.name
import java.io.InputStream
import java.util.Locale
import java.util.TreeMap

private val isoCodes = Locale.getISOLanguages().toSet()

object AvailableLayouts {
    @Composable
    fun listEntries(): List<ListPreferenceEntry<Layout<*>>> = listPrefEntries {
        val embeddedLayouts = rememberEmbeddedLayouts()
        embeddedLayouts.map { entry(it.value, it.key) }
    }
}

@Composable
fun rememberEmbeddedLayouts(): TreeMap<String, EmbeddedLayout> {
    val context = LocalContext.current
    return remember {
        TreeMap(buildMap {
            (R.raw::class.java.fields)
                .filter { isoCodes.contains(it.name) }
                .map { field ->
                    val layout = EmbeddedLayout(field.name)
                    layout
                        .loadKeyboardData(context = context)
                        .fold({ None }, { it ->
                            if (it.totalLayers == 0) {
                                None
                            } else {
                                (it.toString() to layout).some()
                            }
                        })
                }
                .forEach { it.onSome { (k, v) -> put(k, v) } }
        })
    }
}
interface Layout<T> {
    val path: T
    fun inputStream(context: Context): Either<LayoutError, InputStream>
}

fun <T> Layout<T>.loadKeyboardData(context: Context): Either<LayoutError, KeyboardData> {
    val that = this
    return either {
        val stream = inputStream(context = context).bind()

        val keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(
            context.resources,
            stream
        ).bind()
        when (that) {
            is EmbeddedLayout -> {
                KeyboardData.info.name.modify(keyboardData) { name ->
                    name.ifEmpty {
                        val locale = Locale(that.path)
                        Locale.forLanguageTag(that.path).getDisplayName(locale)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                    }
                }

            }

            else -> keyboardData
        }
    }
}

object LayoutSerializer : PreferenceSerializer<Layout<*>> {
    override fun deserialize(value: String): Layout<*>? {
        if (value.length < 2) return null
        val path = value.substring(1)
        return when (value[0]) {
            'e' -> EmbeddedLayout(path)
            'c' -> CustomLayout(Uri.parse(path))
            else -> null
        }
    }

    override fun serialize(value: Layout<*>): String? {
        return when (value) {
            is EmbeddedLayout -> "e${value.path}"
            is CustomLayout -> "c${value.path}"
            else -> null
        }
    }

}

data class EmbeddedLayout(override val path: String) : Layout<String> {
    @SuppressLint("DiscouragedApi")
    override fun inputStream(context: Context): Either<LayoutError, InputStream> {
        val resources = context.resources
        val resourceId =
            resources.getIdentifier(path, "raw", context.packageName)
        return catch({
            resources.openRawResource(resourceId).right()
        }) { e: Throwable -> ExceptionWrapperError(e).left() }
    }
}

class CustomLayout(override val path: Uri) : Layout<Uri> {
    @SuppressLint("Recycle")
    override fun inputStream(context: Context): Either<LayoutError, InputStream> {
        return catch({
            context.contentResolver.openInputStream(path)!!.right()
        }) { e: Throwable -> ExceptionWrapperError(e).left() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomLayout

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
