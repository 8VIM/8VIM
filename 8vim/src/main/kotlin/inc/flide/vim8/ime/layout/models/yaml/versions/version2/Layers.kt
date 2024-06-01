package inc.flide.vim8.ime.layout.models.yaml.versions.version2

import arrow.core.None
import arrow.core.Option
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.ime.layout.models.yaml.versions.common.Action
import inc.flide.vim8.ime.layout.models.yaml.versions.common.ExtraLayer
import inc.flide.vim8.ime.layout.models.yaml.versions.common.Layer
import inc.flide.vim8.ime.layout.models.yaml.versions.version21.Layers as Layers21
import java.util.EnumMap

@optics
data class Layers(
    val hidden: List<Action> = ArrayList(),
    @JsonProperty("default") val defaultLayer: Option<Layer> = None,
    val extraLayers: Map<ExtraLayer, Layer> = EnumMap(ExtraLayer::class.java)
) {
    companion object
}

fun Layers.toLatest(): Layers21 = Layers21(
    hidden = hidden,
    defaultLayer = defaultLayer,
    extraLayers = extraLayers
)
