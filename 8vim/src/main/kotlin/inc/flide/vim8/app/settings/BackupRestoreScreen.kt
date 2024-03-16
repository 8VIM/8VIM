package inc.flide.vim8.app.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import arrow.core.None
import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.core.some
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import inc.flide.vim8.BuildConfig
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.app.availableLayouts
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.ime.layout.toCustomLayout
import inc.flide.vim8.lib.ZipUtils
import inc.flide.vim8.lib.android.readToFile
import inc.flide.vim8.lib.android.showToast
import inc.flide.vim8.lib.android.writeFromFile
import inc.flide.vim8.lib.compose.ErrorCard
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

class Backup(private val context: Context) {
    companion object {
        fun backupFileName(): String {
            val time = System.currentTimeMillis()
            return "backup_${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_CODE}_$time.zip"
        }
    }

    private val prefs by appPreferenceModel()
    private val objectMapper = JsonMapper.builder().build().registerKotlinModule()

    fun export(): File {
        val dir = File(context.cacheDir, UUID.randomUUID().toString())
        val customDir = File(dir, "custom")
        val settings = File(dir, "settings.json")
        val zipFile = File(context.cacheDir, "${UUID.randomUUID()}.zip")
        dir.mkdirs()
        customDir.mkdirs()

        val keys = prefs.exportedKeys.toMutableMap()

        val customs = prefs.layout.custom.history.get().map {
            val layout = it.toCustomLayout()
            val fileName = "${
            layout.md5(context).getOrElse { UUID.randomUUID().toString() }
            }_${layout.defaultName(context)}"
            val file = File(customDir, fileName)
            file.outputStream().use { outStream ->
                layout.inputStream(context).getOrNull()
                    ?.use { inStream -> inStream.copyTo(outStream) }
            }
            it to "custom/$fileName"
        }.associateBy({ it.first }, { it.second })
        val current = keys[prefs.layout.current.key]?.toString()?.substring(1) ?: ""
        if (customs.containsKey(current)) {
            keys[prefs.layout.current.key] = "c${customs[current]}"
        }
        keys[prefs.layout.custom.history.key] = customs.values.toList()

        objectMapper.writeValue(settings, keys)
        ZipUtils.zip(zipFile, dir)
        return zipFile
    }

    @Suppress("Unchecked_cast")
    fun import(uri: Uri): Option<Int> {
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
            return R.string.settings__backup_and_restore__restore__different_version.some()
        }

        val history = (keys[prefs.layout.custom.history.key] as List<String>?)?.let {
            LinkedHashSet(it.map { f -> File(context.filesDir, f).toUri().toString() })
        } ?: emptySet()
        keys[prefs.layout.custom.history.key] = history
        keys[prefs.layout.current.key] = keys[prefs.layout.current.key]?.let {
            history
                .firstOrNone { path -> path.endsWith(it.toString().substring(1)) }
                .map { path -> "c$path" }.getOrElse { it }
        }
        prefs.exportedKeys = keys
        availableLayouts.get()?.reloadCustomLayouts()
        return None
    }
}

@Composable
fun BackupRestoreScreen() = Screen {
    title = stringRes(R.string.settings__backup_and_restore__title)
    previewFieldVisible = false
    val context = LocalContext.current
    val navController = LocalNavController.current
    val backup = Backup(context)
    var errorId by remember { mutableStateOf<Int?>(null) }

    val backupToFileSystemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            runCatching {
                context.contentResolver.writeFromFile(uri, backup.export())
            }
                .onSuccess {
                    context.showToast(R.string.settings__backup_and_restore__back_up__success)
                    navController.popBackStack()
                }
                .onFailure { error ->
                    context.showToast(
                        R.string.settings__backup_and_restore__back_up__failure,
                        "error_message" to error.message
                    )
                }
        }
    )

    val restoreDataFromFileSystemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            runCatching {
                backup.import(uri)
            }.onSuccess {
                it.onSome { id -> errorId = id }
                    .onNone {
                        errorId = null
                        navController.popBackStack()
                    }
            }
                .onFailure { error ->
                    context.showToast(
                        R.string.settings__backup_and_restore__restore__failure,
                        "error_message" to error.message
                    )
                }
        }
    )

    content {
        PreferenceGroup {
            Preference(
                iconId = R.drawable.ic_archive,
                title = stringRes(R.string.settings__backup_and_restore__back_up__title),
                summary = stringRes(
                    R.string.settings__backup_and_restore__back_up__summary
                ),
                onClick = {
                    backupToFileSystemLauncher.launch(Backup.backupFileName())
                }
            )
        }

        PreferenceGroup {
            Preference(
                iconId = R.drawable.ic_settings_backup_restore,
                title = stringRes(R.string.settings__backup_and_restore__restore__title),
                summary = stringRes(
                    R.string.settings__backup_and_restore__restore__summary
                ),
                onClick = {
                    runCatching {
                        errorId = null
                        restoreDataFromFileSystemLauncher.launch("*/*")
                    }.onFailure { error ->
                        context.showToast(
                            R.string.settings__backup_and_restore__restore__failure,
                            "error_message" to error.localizedMessage
                        )
                    }
                }
            )
            if (errorId != null) {
                ErrorCard(
                    modifier = Modifier.padding(8.dp),
                    text = stringRes(errorId!!)
                )
            }
        }
    }
}
