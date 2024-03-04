package inc.flide.vim8.datastore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceModel

typealias PreferenceUiContent<T> = @Composable PreferenceUiScope<T>.() -> Unit

data class PreferenceUiScope<T : PreferenceModel>(
    val prefs: T,
    internal val iconSpaceReserved: Boolean,
    internal val enabledIf: PreferenceDataEvaluator,
    internal val visibleIf: PreferenceDataEvaluator,
    internal val containerColor: Color,
    val columnScope: ColumnScope
) : ColumnScope by columnScope

@Composable
fun <T : PreferenceModel> PreferenceLayout(
    cachedPrefModel: CachedPreferenceModel<T>,
    modifier: Modifier = Modifier,
    content: PreferenceUiContent<T>
) {
    Column(modifier = modifier) {
        val prefModel by cachedPrefModel
        val preferenceScope = PreferenceUiScope(
            prefs = prefModel,
            iconSpaceReserved = true,
            enabledIf = { true },
            visibleIf = { true },
            containerColor = MaterialTheme.colorScheme.surface,
            columnScope = this
        )

        content(preferenceScope)
    }
}
