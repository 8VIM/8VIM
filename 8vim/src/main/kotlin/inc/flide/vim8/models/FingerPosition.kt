package inc.flide.vim8.models

import inc.flide.vim8.models.yaml.ExtraLayer
import inc.flide.vim8.structures.Constants

enum class FingerPosition {
    NO_TOUCH, INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT, LONG_PRESS, LONG_PRESS_END;

    companion object {

        fun computeMovementSequence(
            layer: Int, quadrant: Quadrant,
            position: CharacterPosition
        ): List<FingerPosition> {
            if (layer == Constants.HIDDEN_LAYER) {
                return emptyList()
            }
            val movementSequencesForDefaultLayer =
                movementSequencesForLayer(layer, quadrant, position)
            return if (movementSequencesForDefaultLayer.isEmpty()) {
                movementSequencesForDefaultLayer
            } else {
                movementSequencesForDefaultLayer + INSIDE_CIRCLE
            }
        }

        fun computeQuickMovementSequence(
            layer: Int, quadrant: Quadrant,
            position: CharacterPosition
        ): List<FingerPosition> {
            if (layer <= Constants.DEFAULT_LAYER) {
                return emptyList()
            }
            val movementSequence: MutableList<FingerPosition> = ArrayList()
            val movementSequenceForExtraLayer =
                movementSequenceForExtraLayer(layer, quadrant, position)
            movementSequence.add(INSIDE_CIRCLE)
            movementSequence.addAll(movementSequenceForExtraLayer)
            movementSequence.add(INSIDE_CIRCLE)
            return movementSequence
        }

        private fun movementSequenceForExtraLayer(
            layer: Int, quadrant: Quadrant,
            position: CharacterPosition
        ): List<FingerPosition> {
            val oppositeQuadrant = quadrant.getOppositeQuadrant(position)
            val movementSequence: MutableList<FingerPosition> = ArrayList()
            val maxMovements = position.ordinal + 1
            for (i in 0..maxMovements) {
                val lastPosition =
                    if (movementSequence.isEmpty()) INSIDE_CIRCLE else movementSequence[movementSequence.size - 1]
                val nextPosition = getNextPosition(quadrant, lastPosition)
                movementSequence.add(nextPosition)
            }
            for (i in Constants.DEFAULT_LAYER + 1..layer) {
                val lastPosition =
                    if (movementSequence.isEmpty()) INSIDE_CIRCLE else movementSequence[movementSequence.size - 1]
                val nextPosition = getNextPosition(oppositeQuadrant, lastPosition)
                movementSequence.add(nextPosition)
            }
            return movementSequence
        }

        private fun movementSequencesForLayer(
            layer: Int, quadrant: Quadrant,
            position: CharacterPosition
        ): List<FingerPosition> {
            val maxMovements = position.ordinal + 1
            val movementSequence = if (layer > Constants.DEFAULT_LAYER) {
                val extraLayer = ExtraLayer.values()[layer - 2]
                ExtraLayer.MOVEMENT_SEQUENCES[extraLayer].orEmpty()

            } else {
                emptyList()
            } + INSIDE_CIRCLE

            return (0..maxMovements).fold(movementSequence) { acc, _ ->
                val lastPosition = acc.last()
                val nextPosition = getNextPosition(quadrant, lastPosition)
                acc + nextPosition
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