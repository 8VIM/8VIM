package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState

@Composable
internal fun <T : PreferenceModel> PreferenceUiScope<T>.SwitchPreference(
    pref: PreferenceData<Boolean>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summaryOn: String? = null,
    summaryOff: String? = null,
    summary: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true }
) {
    val prefValue by pref.observeAsState()
    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        Preference(
            modifier = modifier.toggleable(
                value = prefValue,
                enabled = isEnabled,
                role = Role.Switch,
                onValueChange = { pref.set(it) }
            ),
            title = title,
            iconId = iconId,
            iconSpaceReserved = iconSpaceReserved,
            summary = when {
                prefValue && summaryOn != null -> summaryOn
                !prefValue && summaryOff != null -> summaryOff
                summary != null -> summary
                else -> null
            },
            enabledIf = enabledIf,
            trailing = {
                Switch(checked = prefValue, enabled = isEnabled, onCheckedChange = null)
            }
        )
    }
}
