package inc.flide.vim8.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.jetpref.datastore.model.PreferenceSerializer
import inc.flide.vim8.R
import inc.flide.vim8.datastore.ui.ListPreferenceEntry
import inc.flide.vim8.datastore.ui.listPrefEntries
import inc.flide.vim8.keyboardhelpers.InputMethodServiceHelper
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.structures.KeyboardData
import inc.flide.vim8.structures.LayoutFileName
import java.io.InputStream
import java.util.TreeMap

object AvailableLayouts {
    //    private val embeddedLayouts = remember {
//        val context = LocalContext.current
//        val layouts = TreeSet<EmbeddedLayout>()
//        Context applicationContext = context . getApplicationContext ();
//        String[] fields = resources . getStringArray (R.array.keyboard_layouts_id);
//
//        for (String field : fields) {
//        LayoutFileName file = new LayoutFileName(resources, applicationContext, field);
//        if (file.isValidLayout()) {
//            embeddedLayouts.put(file.getLayoutDisplayName(), file.getResourceName());
//        }
//    }
//    }
//
    @Composable
    fun listEntries(): List<ListPreferenceEntry<Layout<*>>> = listPrefEntries {
        val embeddedLayouts = rememberEmbeddedLayouts()
        embeddedLayouts.map { entry(it.value, it.key) }
    }
}

@Composable
fun rememberEmbeddedLayouts(): TreeMap<String, EmbeddedLayout> {
    val context = LocalContext.current
    val layouts = context.resources.getStringArray(R.array.keyboard_layouts_id).map {
        LayoutFileName(context.resources, context, it)
    }.filter { it.isValidLayout }
        .associateBy({ it.layoutDisplayName }, { EmbeddedLayout(it.resourceName) })
    return remember { TreeMap(layouts) }
}


interface Layout<T> {
    val path: T
    fun inputStream(context: Context): InputStream?
}

fun <T> Layout<T>.loadKeyboardData(context: Context): KeyboardData? {
    return tryOrNull {
        inputStream(context = context).let {
            InputMethodServiceHelper.initializeKeyboardActionMap(
                context.resources,
                it
            )
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
            is EmbeddedLayout -> value.path
            is CustomLayout -> value.path.toString()
            else -> null
        }
    }

}

class EmbeddedLayout(override val path: String) : Layout<String> {
    @SuppressLint("DiscouragedApi")
    override fun inputStream(context: Context): InputStream {
        val resources = context.resources
        val resourceId =
            resources.getIdentifier(path, "raw", context.packageName)
        return resources.openRawResource(resourceId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmbeddedLayout

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

class CustomLayout(override val path: Uri) : Layout<Uri> {
    override fun inputStream(context: Context): InputStream? {
        return context.contentResolver.openInputStream(path)
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
