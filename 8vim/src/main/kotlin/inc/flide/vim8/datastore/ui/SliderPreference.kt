package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import kotlin.math.roundToInt

@Composable
internal fun <T : PreferenceModel, V> PreferenceUiScope<T>.SliderPreference(
    pref: PreferenceData<V>,
    modifier: Modifier,
    @DrawableRes iconId: Int?,
    iconSpaceReserved: Boolean,
    title: String,
    summary: String?,
    enabledIf: PreferenceDataEvaluator,
    visibleIf: PreferenceDataEvaluator,
    min: V,
    max: V,
    stepIncrement: V,
    convertToV: (Float) -> V
) where V : Number, V : Comparable<V> {
    val prefValue by pref.observeAsState()
    var sliderValue by remember { mutableFloatStateOf(prefValue.toFloat()) }
    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        Preference(
            title = title,
            iconId = iconId,
            iconSpaceReserved = iconSpaceReserved,
            modifier = modifier,
            summary = summary,
            enabledIf = enabledIf,
            trailing = {
                Text(
                    text = convertToV(sliderValue).toString()
                )
            }
        )

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { pref.set(convertToV(sliderValue)) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = ((max.toFloat() - min.toFloat()) / stepIncrement.toFloat()).toInt() - 1,
            enabled = isEnabled
        )
    }
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.SliderPreference(
    pref: PreferenceData<Int>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
    min: Int,
    max: Int,
    stepIncrement: Int = 1
) {
    SliderPreference(
        pref = pref,
        modifier = modifier,
        iconId = iconId,
        iconSpaceReserved = iconSpaceReserved,
        title = title,
        summary = summary,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        min = min,
        max = max,
        stepIncrement = stepIncrement
    ) {
        try {
            it.roundToInt()
        } catch (e: IllegalArgumentException) {
            it.toInt()
        }
    }
}
