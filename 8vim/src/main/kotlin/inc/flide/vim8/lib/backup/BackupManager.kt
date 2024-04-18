package inc.flide.vim8.lib.backup

import android.content.Context
import android.net.Uri
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.core.getOrNone
import arrow.core.some
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import inc.flide.vim8.BuildConfig
import inc.flide.vim8.R
import inc.flide.vim8.app.availableLayouts
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.ime.layout.toCustomLayout
import inc.flide.vim8.lib.ZipUtils
import inc.flide.vim8.lib.android.fileUri
import inc.flide.vim8.lib.android.readToFile
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

class BackupManager(private val context: Context) {
    companion object {
        fun backupFileName(): String {
            val time = System.currentTimeMillis()
            return "backup_${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_CODE}_$time.zip"
        }
    }

    private val prefs by appPreferenceModel()
    private val objectMapper = JsonMapper.builder().build().registerKotlinModule()

    fun export(): Either<Throwable, File> = Either.catch {
        val dir = File(context.cacheDir, UUID.randomUUID().toString())
        val customDir = File(dir, "custom")
        val settings = File(dir, "settings.json")
        val zipFile = File(context.cacheDir, "${UUID.randomUUID()}.zip")
        dir.mkdirs()
        customDir.mkdirs()

        val keys = prefs.exportedKeys.toMutableMap()

        val customs = LinkedHashMap<String, Option<String>>()
        prefs.layout.custom.history.get().forEach { layoutName ->
            val layout = layoutName.toCustomLayout()
            val fileName = "${
                layout.md5(context).getOrElse { UUID.randomUUID().toString() }
            }_${layout.defaultName(context)}"
            val file = File(customDir, fileName)
            customs[layoutName] = layout.inputStream(context)
                .getOrNone()
                .flatMap {
                    file.outputStream().use { outStream ->
                        it.use { inStream ->
                            inStream.copyTo(outStream)
                            "custom/$fileName".some()
                        }
                    }
                }
        }
        val current = keys[prefs.layout.current.key]?.toString()?.substring(1) ?: ""
        customs.getOrNone(current)
            .onSome { maybePath ->
                keys[prefs.layout.current.key] =
                    maybePath.map { "c$it" }.getOrElse { "e${prefs.layout.current.default.path}" }
            }
        keys[prefs.layout.custom.history.key] = customs.values.flatMap { it.toList() }.toList()

        objectMapper.writeValue(settings, keys)
        ZipUtils.zip(dir, zipFile)
        zipFile
    }

    @Suppress("Unchecked_cast")
    fun import(uri: Uri): Either<Throwable, Option<Int>> = Either.catch {
        val zipFile = File(context.cacheDir, "backup.zip")
        val dstDir = File(context.filesDir, UUID.randomUUID().toString())
        context.contentResolver.readToFile(uri, zipFile)
        ZipUtils.unzip(zipFile, dstDir)

        val keys: MutableMap<String, Any?> = try {
            objectMapper
                .readValue<Map<String, Any?>>(File(dstDir, "settings.json"))
                .toMutableMap()
        } catch (e: FileNotFoundException) {
            error("Invalid archive: either settings.json is missing or file is not a ZIP archive.")
        }

        dstDir.copyRecursively(context.filesDir, overwrite = true)
        if (prefs.version < keys[PreferenceModel.DATASTORE_VERSION] as Int) {
            R.string.settings__backup_and_restore__restore__different_version.some()
        } else {
            val history = (keys[prefs.layout.custom.history.key] as List<String>?)?.let {
                LinkedHashSet(it.map { f -> context.fileUri(f).toString() })
            } ?: emptySet()
            keys[prefs.layout.custom.history.key] = history
            keys[prefs.layout.current.key] = keys[prefs.layout.current.key]?.let {
                history
                    .firstOrNone { path -> path.endsWith(it.toString().substring(1)) }
                    .map { path -> "c$path" }.getOrElse { it }
            }
            prefs.exportedKeys = keys
            availableLayouts.get()?.reloadCustomLayouts()
            None
        }
    }
}
