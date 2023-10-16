package inc.flide.vim8.ime.layout.models

import arrow.core.getOrElse
import arrow.core.lastOrNone
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider

typealias MovementSequence = List<FingerPosition>

class MovementSequenceSerializer : JsonSerializer<MovementSequence>() {
    override fun serialize(
        value: MovementSequence,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeFieldName(value.joinToString(","))
    }
}

class MovementSequenceDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctxt: DeserializationContext): MovementSequence =
        key.split(",").map { FingerPosition.valueOf(it) }
}

enum class FingerPosition {
    NO_TOUCH, INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT, LONG_PRESS, LONG_PRESS_END;

    companion object {

        fun computeMovementSequence(
            layer: LayerLevel,
            quadrant: Quadrant,
            position: CharacterPosition
        ): MovementSequence {
            return when (layer) {
                LayerLevel.HIDDEN -> emptyList()
                else -> {
                    val movementSequencesForDefaultLayer =
                        movementSequencesForLayer(layer, quadrant, position)
                    if (movementSequencesForDefaultLayer.isEmpty()) {
                        emptyList()
                    } else {
                        movementSequencesForDefaultLayer + INSIDE_CIRCLE
                    }
                }
            }
        }

        fun computeQuickMovementSequence(
            layer: LayerLevel,
            quadrant: Quadrant,
            position: CharacterPosition
        ): MovementSequence {
            return when (layer) {
                LayerLevel.HIDDEN -> emptyList()
                else -> {
                    val movementSequenceForExtraLayer =
                        movementSequenceForExtraLayer(layer, quadrant, position)
                    listOf(INSIDE_CIRCLE) + movementSequenceForExtraLayer + listOf(INSIDE_CIRCLE)
                }
            }
        }

        private fun movementSequenceForExtraLayer(
            layer: LayerLevel,
            quadrant: Quadrant,
            position: CharacterPosition
        ): MovementSequence {
            val oppositeQuadrant = quadrant.opposite(position)
            val maxMovements = position.ordinal + 1
            val baseMovementSequence: MovementSequence =
                (0..maxMovements).fold(emptyList()) { acc, _ ->
                    val lastPosition = acc.lastOrNone().getOrElse { INSIDE_CIRCLE }
                    val nextPosition = getNextPosition(quadrant, lastPosition)
                    acc + nextPosition
                }
            return (LayerLevel.SECOND.ordinal..layer.ordinal)
                .fold(baseMovementSequence) { acc, _ ->
                    val lastPosition = acc.lastOrNone().getOrElse { INSIDE_CIRCLE }
                    val nextPosition = getNextPosition(oppositeQuadrant, lastPosition)
                    acc + nextPosition
                }
        }

        private fun movementSequencesForLayer(
            layer: LayerLevel,
            quadrant: Quadrant,
            position: CharacterPosition
        ): MovementSequence {
            return when (layer) {
                LayerLevel.HIDDEN -> emptyList()
                else -> {
                    val maxMovements = position.ordinal + 1
                    val movementSequence =
                        LayerLevel.MovementSequences[layer].orEmpty() + INSIDE_CIRCLE
                    (0..maxMovements).fold(movementSequence) { acc, _ ->
                        val lastPosition = acc.last()
                        val nextPosition = getNextPosition(quadrant, lastPosition)
                        acc + nextPosition
                    }
                }
            }
        }

        private fun getNextPosition(
            quadrant: Quadrant,
            lastPosition: FingerPosition
        ): FingerPosition {
            val currentSector: FingerPosition = quadrant.sector.toFingerPosition()
            val oppositeSector: FingerPosition = quadrant.sector.opposite().toFingerPosition()
            val currentPart: FingerPosition = quadrant.part.toFingerPosition()
            val oppositePart: FingerPosition = quadrant.part.opposite().toFingerPosition()
            return if (lastPosition === INSIDE_CIRCLE || lastPosition === oppositePart) {
                currentSector
            } else if (lastPosition === oppositeSector) {
                oppositePart
            } else if (lastPosition === currentPart) {
                oppositeSector
            } else {
                currentPart
            }
        }
    }
}
