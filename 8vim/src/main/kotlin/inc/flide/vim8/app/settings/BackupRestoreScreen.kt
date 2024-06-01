package inc.flide.vim8.app.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import arrow.core.Either
import arrow.core.flatMap
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.backupManager
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.lib.android.showToast
import inc.flide.vim8.lib.android.writeFromFile
import inc.flide.vim8.lib.backup.BackupManager
import inc.flide.vim8.lib.compose.ErrorCard
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun BackupRestoreScreen() = Screen {
    title = stringRes(R.string.settings__backup_and_restore__title)
    previewFieldVisible = false
    val context = LocalContext.current
    val navController = LocalNavController.current
    val backupManager by context.backupManager()
    var errorId by remember { mutableStateOf<Int?>(null) }
    var confirmDialogOpened by remember { mutableStateOf(false) }

    val backupToFileSystemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            backupManager.export()
                .flatMap { Either.catch { context.contentResolver.writeFromFile(uri, it) } }
                .onRight {
                    context.showToast(R.string.settings__backup_and_restore__back_up__success)
                    navController.popBackStack()
                }
                .onLeft {
                    context.showToast(
                        R.string.settings__backup_and_restore__back_up__failure,
                        "error_message" to it.message
                    )
                }
        }
    )

    val restoreDataFromFileSystemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            backupManager.import(uri)
                .onLeft {
                    context.showToast(
                        R.string.settings__backup_and_restore__restore__failure,
                        "error_message" to it.message
                    )
                }
                .onRight {
                    it.onSome { id -> errorId = id }
                        .onNone {
                            errorId = null
                            context.showToast(
                                R.string.settings__backup_and_restore_restore__success
                            )
                            navController.popBackStack()
                        }
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
                    backupToFileSystemLauncher.launch(BackupManager.backupFileName())
                }
            )

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

            Preference(
                iconId = R.drawable.ic_reset,
                title = stringRes(R.string.settings__backup_and_restore__reset__title),
                onClick = { if (!confirmDialogOpened) confirmDialogOpened = true }
            )
            if (confirmDialogOpened) {
                AlertDialog(
                    title = { Text(stringRes(R.string.dialog__confirm__title)) },
                    text = { Text(stringRes(R.string.resetting_settings__description)) },
                    onDismissRequest = { confirmDialogOpened = false },
                    confirmButton = {
                        TextButton(onClick = {
                            prefs.reset()
                            confirmDialogOpened = false
                            context.showToast(R.string.settings__backup_and_restore__reset)
                            navController.popBackStack()
                        }) {
                            Text(stringRes(R.string.dialog__confirm__label))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDialogOpened = false }) {
                            Text(stringRes(R.string.dialog__dismiss__label))
                        }
                    }
                )
            }
        }
    }
}
