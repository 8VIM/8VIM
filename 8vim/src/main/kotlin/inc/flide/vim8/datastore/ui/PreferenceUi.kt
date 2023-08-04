package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceDataEvaluator
import inc.flide.vim8.datastore.model.PreferenceDataEvaluatorScope
import inc.flide.vim8.datastore.model.PreferenceModel

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class PreferenceUiScopeDsl

typealias PreferenceUiContent<T> = @Composable @PreferenceUiScopeDsl PreferenceUiScope<T>.() -> Unit

class PreferenceUiScope<T : PreferenceModel>(
    val prefs: T,
    internal val iconSpaceReserved: Boolean,
    internal val enabledIf: PreferenceDataEvaluator,
    internal val visibleIf: PreferenceDataEvaluator,
    columnScope: ColumnScope,
) : ColumnScope by columnScope


@Composable
fun <T : PreferenceModel> PreferenceLayout(
    cachedPrefModel: CachedPreferenceModel<T>,
    modifier: Modifier = Modifier,
    iconSpaceReserved: Boolean = true,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    Column(modifier = modifier) {
        val prefModel by cachedPrefModel
        val preferenceScope = PreferenceUiScope(
            prefs = prefModel,
            iconSpaceReserved = iconSpaceReserved,
            enabledIf = enabledIf,
            visibleIf = visibleIf,
            columnScope = this,
        )

        content(preferenceScope)
    }
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.PreferenceGroup(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    content: PreferenceUiContent<T>,
) {
    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        Column(
            modifier,
        ) {
            val preferenceScope = PreferenceUiScope(
                prefs = this@PreferenceGroup.prefs,
                iconSpaceReserved = iconSpaceReserved,
                enabledIf = { this@PreferenceGroup.enabledIf(evalScope) && enabledIf(evalScope) },
                visibleIf = { this@PreferenceGroup.visibleIf(evalScope) && visibleIf(evalScope) },
                columnScope = this@Column,
            )

            ListItem(
                leadingContent = maybeJetIcon(iconId, iconSpaceReserved),
                headlineContent = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },
            )
            content(preferenceScope)
        }
    }
}
