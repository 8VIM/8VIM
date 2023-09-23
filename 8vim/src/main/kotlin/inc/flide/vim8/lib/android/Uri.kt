package inc.flide.vim8.lib.android

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.core.net.toFile
import java.io.File

private val contentUriPrefixesToTry = arrayOf(
    "content://downloads/public_downloads",
    "content://downloads/all_downloads",
    "content://downloads/my_downloads"
)

fun Uri.toFile(context: Context): File? {
    return when (scheme) {
        "file" -> this.toFile()
        "content" -> contentUriToFile(context)
        else -> null
    }
}

private fun Uri.contentUriToFile(context: Context): File? {
    return when (authority) {
        "com.android.externalstorage.documents" -> externalStorageProviderToFile(this, context)
        "com.android.providers.downloads.documents" -> documentProviderToFile(this, context)

        else -> null
    }
}

private fun externalStorageProviderToFile(uri: Uri, context: Context): File? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":")
    val type = split[0]
    return if (type.equals("primary", true)) {
        File("${Environment.getExternalStorageDirectory()}/${split[1]}")
    } else {
        context.systemServiceOrNull(StorageManager::class)?.let { storageManager ->
            storageManager.storageVolumes.forEach { storageVolume ->
                storageVolume.isEmulated
                val mounted =
                    storageVolume.state == Environment.MEDIA_MOUNTED ||
                        storageVolume.state == Environment.MEDIA_MOUNTED_READ_ONLY
                val isPrimaryHandled = storageVolume.isPrimary && storageVolume.isEmulated
                if (mounted && !isPrimaryHandled) {
                    storageVolume.uuid?.let {
                        if (it == type) {
                            storageVolume.path()
                        } else {
                            null
                        }
                    }?.let {
                        return File(it, split[1])
                    }
                }
            }
        }
        null
    }
}

private fun documentProviderToFile(uri: Uri, context: Context): File? {
    var id = DocumentsContract.getDocumentId(uri)
    when {
        TextUtils.isEmpty(id) -> return null
        id.startsWith("raw:") -> return File(id.substring(4))
        id.startsWith("msf:") -> id = id.split(":")[1]
    }
    val availableId = tryOrNull { id.toLong() } ?: return null
    contentUriPrefixesToTry.forEach {
        val contentUri = ContentUris.withAppendedId(Uri.parse(it), availableId)
        getFileFromUri(contentUri, context)?.let { file -> return file }
    }
    return null
}

fun getFileFromUri(contentUri: Uri, context: Context): File? {
    return when (contentUri.authority) {
        "com.tencent.mtt.fileprovider" -> if (TextUtils.isEmpty(contentUri.path)) {
            null
        } else {
            val fileDir = Environment.getExternalStorageDirectory()
            contentUri.path?.let { it.substring("/QQBrowser".length, it.length) }?.let {
                File(
                    fileDir,
                    it
                )
            }
        }

        else ->
            context.contentResolver
                .query(contentUri, arrayOf("_data"), null, null, null)?.let {
                    try {
                        if (!it.moveToFirst()) {
                            null
                        } else {
                            val columnIndex = it.getColumnIndex("_data")
                            if (columnIndex > -1) {
                                File(it.getString(columnIndex))
                            } else {
                                null
                            }
                        }
                    } catch (_: Throwable) {
                        null
                    } finally {
                        it.close()
                    }
                }
    }
}

private fun StorageVolume.path(): File? {
    return if (AndroidVersion.ATLEAST_API30_R) {
        directory
    } else {
        val cls = StorageVolume::class.java
        val getPath = cls.getMethod("getPath")
        File(getPath.invoke(this) as String)
    }
}
