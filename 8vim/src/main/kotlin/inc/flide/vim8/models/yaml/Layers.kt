package inc.flide.vim8.models.yaml

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.EnumMap

@optics
data class Layers(
    val hidden: List<Action> = ArrayList(),
    @JsonProperty("default") val defaultLayer: Option<Layer> = None,
    val extraLayers: Map<ExtraLayer, Layer> = EnumMap(ExtraLayer::class.java)
) {
    companion object
}

fun Layers.hasOnlyHiddenLayer(): Boolean = extraLayers.isNotEmpty() && defaultLayer.isNone()
