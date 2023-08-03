package inc.flide.vim8.datastore.ui

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.MaterialSwitchPreference(
    pref: PreferenceData<Boolean>,
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    summaryOn: String? = null,
    summaryOff: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val prefValue by pref.observeAsState()

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            modifier = modifier
                .toggleable(
                    value = prefValue,
                    enabled = isEnabled,
                    role = Role.Switch,
                    onValueChange = { pref.set(it) }
                ),
            text = title,
            secondaryText = when {
                prefValue && summaryOn != null -> summaryOn
                !prefValue && summaryOff != null -> summaryOff
                summary != null -> summary
                else -> null
            },
            trailingContent = {
                Switch(
                    checked = prefValue,
                    onCheckedChange = null,
                    enabled = isEnabled
                )
            },
            enabled = isEnabled,
        )
    }
}
