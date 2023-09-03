package inc.flide.vim8.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.none
import arrow.core.raise.catch
import arrow.core.right
import inc.flide.vim8.R
import inc.flide.vim8.datastore.model.PreferenceSerDe
import inc.flide.vim8.ime.LayoutLoader
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.models.error.ExceptionWrapperError
import inc.flide.vim8.models.error.LayoutError
import inc.flide.vim8.models.yaml.name
import java.io.InputStream
import java.util.Locale
import java.util.TreeMap

private val isoCodes = Locale.getISOLanguages().toSet()

fun embeddedLayouts(context: Context): TreeMap<String, EmbeddedLayout> {
    return TreeMap(
        buildMap {
            (R.raw::class.java.fields)
                .filter { isoCodes.contains(it.name) }
                .map { field ->
                    EmbeddedLayout(field.name)
                        .let { layout ->
                            layout
                                .loadKeyboardData(context)
                                .getOrNone()
                                .filterNot { it.totalLayers == 0 }
                                .map { it.toString() to layout }
                        }
                }
                .forEach { it.onSome { (k, v) -> put(k, v) } }
        }
    )
}

@Composable
fun rememberEmbeddedLayouts(): TreeMap<String, EmbeddedLayout> {
    val context = LocalContext.current
    return remember { embeddedLayouts(context) }
}

interface Layout<T> {
    val path: T
    fun inputStream(context: Context): Either<LayoutError, InputStream>
}

fun <T> Layout<T>.loadKeyboardData(context: Context): Either<LayoutError, KeyboardData> {
    return inputStream(context)
        .flatMap { LayoutLoader.loadKeyboardData(context.resources, it) }
        .map {
            KeyboardData.info.name.modify(it) { name ->
                name.ifEmpty {
                    when (this) {
                        is EmbeddedLayout -> this.defaultName()
                        is CustomLayout -> this.defaultName(context)
                        else -> ""
                    }
                }
            }
        }
}

private fun EmbeddedLayout.defaultName(): String {
    val locale = Locale(path)
    return Locale.forLanguageTag(path).getDisplayName(locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

private fun CustomLayout.defaultName(context: Context): String {
    return Option.catch {
        Option.fromNullable(path.scheme)
            .flatMap {
                when (it) {
                    "file" -> Option.fromNullable(path.lastPathSegment)
                    "content" -> Option.fromNullable(
                        context.contentResolver.query(
                            path,
                            null,
                            null,
                            null,
                            null
                        )
                    ).map { cursor ->
                        var result = ""
                        if (cursor.count != 0) {
                            val columnIndex =
                                cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            result = cursor.getString(columnIndex)
                        }
                        cursor.close()
                        result
                    }

                    else -> none()
                }
            }
    }.flatten().getOrElse { "" }
}

fun String.toCustomLayout(): CustomLayout {
    return CustomLayout(Uri.parse(this))
}

object LayoutSerDe : PreferenceSerDe<Layout<*>> {
    override fun serialize(
        editor: SharedPreferences.Editor,
        key: String,
        value: Layout<*>
    ) {
        val encodedValue = when (value) {
            is EmbeddedLayout -> "e${value.path}"
            is CustomLayout -> "c${value.path}"
            else -> null
        }

        editor.putString(key, encodedValue)
    }

    override fun deserialize(
        sharedPreferences: SharedPreferences,
        key: String,
        default: Layout<*>
    ): Layout<*> {
        return tryOrNull { sharedPreferences.getString(key, null) }
            ?.let { deserialize(it) } ?: default
    }

    override fun deserialize(value: Any?): Layout<*>? {
        return value?.toString()?.let {
            if (it.length >= 2) {
                val path = it.substring(1)
                when (it[0]) {
                    'e' -> EmbeddedLayout(path)
                    'c' -> CustomLayout(Uri.parse(path))
                    else -> null
                }
            } else {
                null
            }
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

data class CustomLayout(override val path: Uri) : Layout<Uri> {
    @SuppressLint("Recycle")
    override fun inputStream(context: Context): Either<LayoutError, InputStream> {
        return catch({
            context.contentResolver.openInputStream(path)!!.right()
        }) { e: Throwable -> ExceptionWrapperError(e).left() }
    }
}
