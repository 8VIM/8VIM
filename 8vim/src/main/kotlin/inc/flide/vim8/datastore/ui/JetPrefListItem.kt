package inc.flide.vim8.datastore.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
internal inline fun whenNotNullOrBlank(
    string: String?,
    crossinline composer: @Composable (text: String) -> Unit,
): @Composable (() -> Unit)? {
    return when {
        !string.isNullOrBlank() -> ({ composer(string) })
        else -> null
    }
}

@Composable
fun JetPrefListItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    overlineText: String? = null,
    text: String,
    secondaryText: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier.alpha(if (enabled) 1.0f else 0.38f),
        leadingContent = icon,
        overlineContent = whenNotNullOrBlank(overlineText) { str ->
            Text(text = str)
        },
        headlineContent = { Text(text = text) },
        supportingContent = whenNotNullOrBlank(secondaryText) { str ->
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = str,
            )
        },
        trailingContent = trailing,
    )
}
