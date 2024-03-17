package inc.flide.vim8.lib.android

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun ContentResolver.read(
    uri: Uri,
    maxSize: Long = Long.MAX_VALUE,
    block: (InputStream) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    require(maxSize > 0) { "Argument `maxSize` must be greater than 0" }
    val inputStream = this.openInputStream(uri)
        ?: error("Cannot open input stream for given uri '$uri'")
    val assetFileDescriptor = this.openAssetFileDescriptor(uri, "r")
        ?: error("Cannot open asset file descriptor for given uri '$uri'")
    assetFileDescriptor.use {
        val assetFileSize = assetFileDescriptor.length
        if (assetFileSize != AssetFileDescriptor.UNKNOWN_LENGTH) {
            if (assetFileSize > maxSize) {
                error("Contents of given uri '$uri' exceeds maximum size of $maxSize bytes!")
            }
        }
    }
    inputStream.use(block)
}

fun ContentResolver.readToFile(uri: Uri, file: File) {
    this.read(uri) { inStream ->
        file.outputStream().use { outStream ->
            inStream.copyTo(outStream)
        }
    }
}

inline fun ContentResolver.write(uri: Uri, block: (OutputStream) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val outputStream = this.openOutputStream(uri, "wt")
        ?: error("Cannot open input stream for given uri '$uri'")
    outputStream.use(block)
}

fun ContentResolver.writeFromFile(uri: Uri, file: File) {
    this.write(uri) { outStream ->
        file.inputStream().use { inStream ->
            inStream.copyTo(outStream)
        }
    }
}
