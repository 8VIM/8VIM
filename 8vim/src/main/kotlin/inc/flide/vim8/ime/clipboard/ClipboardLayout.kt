package inc.flide.vim8.ime.clipboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import inc.flide.vim8.clipboardManager
import inc.flide.vim8.editorInstance
import inc.flide.vim8.lib.compose.verticalScroll
import inc.flide.vim8.lib.observeAsNonNullState

@Composable
fun ClipboardLayout() {
    val context = LocalContext.current
    val clipboardManager by context.clipboardManager()
    val editorInstance by context.editorInstance()

    val history by clipboardManager.history.observeAsNonNullState()

    Column(modifier = Modifier.verticalScroll()) {
        for ((idx, item) in history.withIndex()) {
            ListItem(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = { editorInstance.commitText(item) }
                ),
                headlineContent = { Text("${idx + 1}. $item") }
            )
        }
    }
}
