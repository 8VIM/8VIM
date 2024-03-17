@file:Suppress("NOTHING_TO_INLINE")

package inc.flide.vim8.lib.android

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import inc.flide.vim8.lib.kotlin.CurlyArg
import inc.flide.vim8.lib.kotlin.curlyFormat
import java.io.File
import kotlin.reflect.KClass

@Throws(NullPointerException::class, ClassCastException::class)
fun <T : Any> Context.systemService(kClass: KClass<T>): T {
    val serviceName = this.getSystemServiceName(kClass.java)!!
    @Suppress("UNCHECKED_CAST")
    return this.getSystemService(serviceName) as T
}

fun <T : Any> Context.systemServiceOrNull(kClass: KClass<T>): T? {
    return try {
        this.systemService(kClass)
    } catch (e: Exception) {
        null
    }
}

@Throws(android.content.res.Resources.NotFoundException::class)
inline fun Context.stringRes(@StringRes id: Int, vararg args: CurlyArg): String {
    return this.resources.getString(id).curlyFormat(*args)
}

fun Context.fileUri(name: String): Uri = File(filesDir, name).toUri()
