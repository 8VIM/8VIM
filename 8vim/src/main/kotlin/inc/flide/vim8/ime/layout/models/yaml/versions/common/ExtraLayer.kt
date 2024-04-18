package inc.flide.vim8.ime.layout.models.yaml.versions.common

import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.LayerLevel

enum class ExtraLayer {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    FIFTH;

    companion object {
        val MOVEMENT_SEQUENCES: Map<ExtraLayer, List<FingerPosition>> = entries.fold(
            listOf(
                FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE
            ) to mapOf<ExtraLayer, List<FingerPosition>>()
        ) { (movementSequence, acc), extraLayer ->

            val newMovementSequence = when (extraLayer) {
                FIRST, FIFTH -> movementSequence + (FingerPosition.BOTTOM)
                SECOND -> movementSequence + (FingerPosition.LEFT)
                THIRD -> movementSequence + (FingerPosition.TOP)
                FOURTH -> movementSequence + (FingerPosition.RIGHT)
            }
            val newMap = acc + (extraLayer to newMovementSequence)
            newMovementSequence to newMap
        }.second
    }
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
