package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import arrow.core.Option
import arrow.core.getOrElse
import inc.flide.vim8.datastore.model.PreferenceModel

@Composable
internal inline fun whenNotNullOrBlank(
    string: String?,
    crossinline composer: @Composable (text: String) -> Unit
): @Composable (() -> Unit)? {
    return when {
        !string.isNullOrBlank() -> ({ composer(string) })
        else -> null
    }
}

@Composable
internal fun maybeIcon(
    @DrawableRes id: Int?,
    iconSpaceReserved: Boolean,
    contentDescription: String? = null
): @Composable (() -> Unit)? {
    return when {
        id != null -> (
            {
                Icon(
                    painter = painterResource(id),
                    contentDescription = contentDescription
                )
            }
            )

        iconSpaceReserved -> ({ })
        else -> null
    }
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.PreferenceGroup(
    content: PreferenceUiContent<T>
) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            content(
                this@PreferenceGroup.copy(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.Preference(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    onClick: (() -> Unit)? = null
) {
    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        ListItem(
            modifier = Option
                .fromNullable(onClick)
                .map { modifier.clickable(enabled = isEnabled, role = Role.Button, onClick = it) }
                .getOrElse { modifier }
                .alpha(if (isEnabled) 1.0f else 0.38f),
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = whenNotNullOrBlank(summary) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            leadingContent = maybeIcon(iconId, iconSpaceReserved),
            trailingContent = trailing,
            colors = ListItemDefaults.colors(containerColor = this.containerColor)
        )
    }
}
