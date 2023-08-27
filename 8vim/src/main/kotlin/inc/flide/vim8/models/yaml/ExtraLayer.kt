package inc.flide.vim8.models.yaml

import inc.flide.vim8.models.LayerLevel

enum class ExtraLayer {
    FIRST, SECOND, THIRD, FOURTH, FIFTH;
}

fun ExtraLayer.toLayerLevel(): LayerLevel {
    return when (this) {
        ExtraLayer.FIRST -> LayerLevel.SECOND
        ExtraLayer.SECOND -> LayerLevel.THIRD
        ExtraLayer.THIRD -> LayerLevel.FOURTH
        ExtraLayer.FOURTH -> LayerLevel.FIFTH
        ExtraLayer.FIFTH -> LayerLevel.SIXTH
    }
}
