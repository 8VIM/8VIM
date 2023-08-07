package inc.flide.vim8.datastore.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceModel

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class PreferenceUiScopeDsl

typealias PreferenceUiContent<T> = @Composable @PreferenceUiScopeDsl PreferenceUiScope<T>.() -> Unit

class PreferenceUiScope<T : PreferenceModel>(
    val prefs: T,
    columnScope: ColumnScope,
) : ColumnScope by columnScope

@Composable
fun <T : PreferenceModel> PreferenceLayout(
    cachedPrefModel: CachedPreferenceModel<T>,
    modifier: Modifier = Modifier,
    content: PreferenceUiContent<T>,
) {
    Column(modifier = modifier) {
        val prefModel by cachedPrefModel
        val preferenceScope = PreferenceUiScope(
            prefs = prefModel,
            columnScope = this,
        )

        content(preferenceScope)
    }
}
