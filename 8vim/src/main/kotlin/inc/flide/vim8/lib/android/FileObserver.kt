package inc.flide.vim8.lib.android

import android.os.FileObserver
import java.io.File

fun FileObserver(
    file: File,
    mask: Int,
    onEvent: (event: Int, path: String?) -> Unit
): FileObserver {
    return if (AndroidVersion.ATLEAST_API29_Q) {
        object : FileObserver(file, mask) {
            override fun onEvent(event: Int, path: String?) = onEvent(event, path)
        }
    } else {
        @Suppress("DEPRECATION")
        object : FileObserver(file.absolutePath, mask) {
            override fun onEvent(event: Int, path: String?) = onEvent(event, path)
        }
    }
}
