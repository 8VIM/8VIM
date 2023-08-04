package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import inc.flide.vim8.datastore.model.PreferenceDataEvaluator
import inc.flide.vim8.datastore.model.PreferenceDataEvaluatorScope
import inc.flide.vim8.datastore.model.PreferenceModel

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
    onClick: (() -> Unit)? = null,
) {
    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            modifier = if (onClick != null) {
                modifier.clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = onClick,
                )
            } else {
                modifier
            },
            leadingContent = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = summary,
            trailingContent = trailing,
            enabled = isEnabled,
        )
    }
}
