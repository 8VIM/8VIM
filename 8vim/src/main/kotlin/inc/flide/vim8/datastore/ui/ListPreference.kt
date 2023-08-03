package inc.flide.vim8.datastore.ui

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.DialogPrefStrings
import dev.patrickgold.jetpref.datastore.ui.LocalDefaultDialogPrefStrings
import inc.flide.vim8.lib.compose.MaterialDialog
import inc.flide.vim8.lib.compose.verticalScroll

data class ListPreferenceEntry<V : Any>(
    val key: V,
    val label: String,
    val labelComposer: @Composable (String) -> Unit,
    val description: String,
    val descriptionComposer: @Composable (String) -> Unit,
    val showDescriptionOnlyIfSelected: Boolean,
)

interface ListPreferenceEntriesScope<V : Any> {

    fun entry(key: V, label: String)

    fun entry(
        key: V,
        label: String,
        description: String,
        showDescriptionOnlyIfSelected: Boolean = false
    )

    fun entry(
        key: V,
        label: String,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )

    fun entry(
        key: V,
        label: String,
        labelComposer: @Composable (String) -> Unit,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean = false,
    )
}

private class ListPreferenceEntriesScopeImpl<V : Any> : ListPreferenceEntriesScope<V> {
    private val entries = mutableListOf<ListPreferenceEntry<V>>()

    override fun entry(key: V, label: String) {
        entries.add(
            ListPreferenceEntry(
                key = key,
                label = label,
                labelComposer = { Text(it) },
                description = "",
                descriptionComposer = { },
                showDescriptionOnlyIfSelected = false
            )
        )
    }

    override fun entry(
        key: V,
        label: String,
        description: String,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(
            ListPreferenceEntry(
                key = key,
                label = label,
                labelComposer = { Text(it) },
                description = description,
                descriptionComposer = { Text(it, style = MaterialTheme.typography.bodyMedium) },
                showDescriptionOnlyIfSelected = showDescriptionOnlyIfSelected
            )
        )
    }

    override fun entry(
        key: V,
        label: String,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(
            ListPreferenceEntry(
                key = key,
                label = label,
                labelComposer = { Text(it) },
                description = description,
                descriptionComposer = descriptionComposer,
                showDescriptionOnlyIfSelected = showDescriptionOnlyIfSelected
            )
        )
    }

    override fun entry(
        key: V,
        label: String,
        labelComposer: @Composable (String) -> Unit,
        description: String,
        descriptionComposer: @Composable (String) -> Unit,
        showDescriptionOnlyIfSelected: Boolean,
    ) {
        entries.add(
            ListPreferenceEntry(
                key = key,
                label = label,
                labelComposer = labelComposer,
                description = description,
                descriptionComposer = descriptionComposer,
                showDescriptionOnlyIfSelected = showDescriptionOnlyIfSelected
            )
        )
    }

    fun build(): List<ListPreferenceEntry<V>> {
        return entries.toList()
    }
}

@Composable
fun <V : Any> listPrefEntries(
    scope: @Composable ListPreferenceEntriesScope<V>.() -> Unit,
): List<ListPreferenceEntry<V>> {
    val builder = ListPreferenceEntriesScopeImpl<V>()
    scope(builder)
    return builder.build()
}

@Composable
fun <T : PreferenceModel, V : Any> PreferenceUiScope<T>.ListPreference(
    listPref: PreferenceData<V>,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    entries: List<ListPreferenceEntry<V>>,
) {
    val listPrefValue by listPref.observeAsState()
    val (tmpListPrefValue, setTmpListPrefValue) = remember { mutableStateOf(listPref.get()) }
    val isDialogOpen = remember { mutableStateOf(false) }

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            modifier = modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        setTmpListPrefValue(listPrefValue)
                        isDialogOpen.value = true
                    }
                ),
            leadingContent = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = entries.find {
                it.key == listPrefValue
            }?.label ?: "!! invalid !!",
            enabled = isEnabled,
        )
        if (isDialogOpen.value) {
            MaterialDialog(
                title = title,
                dismissText = dialogStrings.dismissLabel,
                onDismiss = { isDialogOpen.value = false },
                confirmText = dialogStrings.confirmLabel,
                onConfirm = {
                    listPref.set(tmpListPrefValue)
                    isDialogOpen.value = false
                }) {
                Column(modifier = Modifier.verticalScroll()) {
                    for (entry in entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = entry.key == tmpListPrefValue,
                                    onClick = {
                                        setTmpListPrefValue(entry.key)
                                    }
                                )
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                )
                        ) {
                            RadioButton(
                                selected = entry.key == tmpListPrefValue,
                                onClick = null,
                                modifier = Modifier.padding(end = 12.dp),
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                entry.labelComposer(entry.label)
                                if (entry.showDescriptionOnlyIfSelected) {
                                    if (entry.key == tmpListPrefValue) {
                                        entry.descriptionComposer(entry.description)
                                    }
                                } else {
                                    entry.descriptionComposer(entry.description)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}