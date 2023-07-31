package inc.flide.vim8.lib.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun MaterialDialog(
    title: String,
    dismissText: String,
    onDismiss: () -> Unit,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val dismissButton = @Composable {
        TextButton(onClick = onDismiss) {
            Text(text = dismissText)
        }
    }
    val confirmButton = confirmText?.let {
        @Composable {
            TextButton(onClick = onConfirm ?: {}, enabled = onConfirm != null) {
                Text(text = it)
            }
        }
    } ?: dismissButton
    AlertDialog(
        title = { Text(text = title) },
        onDismissRequest = onDismiss,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}