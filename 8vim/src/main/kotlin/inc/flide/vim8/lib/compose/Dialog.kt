package inc.flide.vim8.lib.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import inc.flide.vim8.R

@Composable
fun Dialog(builder: @Composable DialogScope.() -> Unit) {
    val scope = remember { DialogScopeImpl() }
    builder(scope)
    scope.Render()
}

typealias DialogOnConfirm = (which: Int) -> Unit
typealias DialogIndex = @Composable () -> Int
typealias DialogItems = @Composable () -> Collection<String>

interface DialogScope {
    var title: String

    var index: DialogIndex
    var items: DialogItems

    fun onConfirm(callback: DialogOnConfirm)
    fun show()
}

private class DialogScopeImpl : DialogScope {
    override var title: String by mutableStateOf("")
    override var index: DialogIndex = { 0 }
    override var items: DialogItems = { emptyList() }

    private var callback: DialogOnConfirm = {}
    private var openAlertDialog by mutableStateOf(false)

    override fun onConfirm(callback: DialogOnConfirm) {
        this.callback = callback
    }

    override fun show() {
        if (!openAlertDialog) openAlertDialog = true
    }

    @Composable
    fun Render() {
        val currentIndex = index()
        var selectedIndex by remember { mutableIntStateOf(currentIndex) }
        if (openAlertDialog) {
            selectedIndex = currentIndex
            AlertDialog(
                title = { Text(title) },
                onDismissRequest = { openAlertDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            callback(selectedIndex)
                            openAlertDialog = false
                        }
                    ) {
                        Text(stringRes(R.string.generic_okay_text))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openAlertDialog = false }) {
                        Text(stringRes(R.string.generic_cancel_text))
                    }
                },
                text = {
                    Column(modifier = Modifier.verticalScroll()) {
                        for ((index, item) in items().withIndex()) {
                            ListItem(
                                modifier = Modifier.clickable(role = Role.RadioButton) {
                                    selectedIndex = index
                                },
                                headlineContent = { Text(item) },
                                leadingContent = {
                                    RadioButton(
                                        selected = index == selectedIndex,
                                        onClick = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
