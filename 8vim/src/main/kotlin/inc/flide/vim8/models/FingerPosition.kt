package inc.flide.vim8.models

import arrow.core.getOrElse
import arrow.core.lastOrNone

typealias MovementSequence = List<Int>

object FingerPosition {
    const val NO_TOUCH: Int = -1
    const val LONG_PRESS: Int = -2
    const val LONG_PRESS_END: Int = -3
    const val INSIDE_CIRCLE: Int = 0
    const val BOTTOM: Int = 1
    const val LEFT: Int = 2
    const val TOP: Int = 3
    const val RIGHT: Int = 4

    @JvmStatic
    fun getDirection(start: Int, end: Int, sectors: Int): Int {
        var direction = (end - start) % sectors
        if (direction < 0)
            direction += sectors
        return if (direction != 1) -1 else direction
    }

    fun computeMovementSequence(
        layer: LayerLevel,
        quadrant: Quadrant,
        position: CharacterPosition,
        keyboardData: KeyboardData
    ): MovementSequence {
        return when (layer) {
            LayerLevel.HIDDEN -> emptyList()
            else -> {
                val movementSequencesForDefaultLayer =
                    movementSequencesForLayer(layer, quadrant, position, keyboardData)
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
        position: CharacterPosition,
        keyboardData: KeyboardData
    ): MovementSequence {
        return when (layer) {
            LayerLevel.HIDDEN -> emptyList()
            else -> {
                val movementSequenceForExtraLayer =
                    movementSequenceForExtraLayer(layer, quadrant, position, keyboardData)
                listOf(INSIDE_CIRCLE) + movementSequenceForExtraLayer + listOf(INSIDE_CIRCLE)
            }
        }
    }

    private fun movementSequenceForExtraLayer(
        layer: LayerLevel,
        quadrant: Quadrant,
        position: CharacterPosition,
        keyboardData: KeyboardData
    ): MovementSequence {
        val oppositeQuadrant = quadrant.opposite(position, keyboardData)
        val maxMovements = position.ordinal + 1
        val baseMovementSequence: MovementSequence =
            (0..maxMovements).fold(emptyList()) { acc, _ ->
                val lastPosition = acc.lastOrNone().getOrElse { INSIDE_CIRCLE }
                val nextPosition = getNextPosition(quadrant, lastPosition, keyboardData)
                acc + nextPosition
            }
        return (LayerLevel.SECOND.ordinal..layer.ordinal)
            .fold(baseMovementSequence) { acc, _ ->
                val lastPosition = acc.lastOrNone().getOrElse { INSIDE_CIRCLE }
                val nextPosition = getNextPosition(oppositeQuadrant, lastPosition, keyboardData)
                acc + nextPosition
            }
    }

    private fun movementSequencesForLayer(
        layer: LayerLevel,
        quadrant: Quadrant,
        position: CharacterPosition,
        keyboardData: KeyboardData
    ): MovementSequence {
        return when (layer) {
            LayerLevel.HIDDEN -> emptyList()
            else -> {
                val maxMovements = position.ordinal + 1
                val movementSequence =
                    LayerLevel.movementSequences[layer].orEmpty() + INSIDE_CIRCLE
                val direction = getDirection(quadrant.sector, quadrant.part, keyboardData.sectors)
                return (0..maxMovements).fold(movementSequence to quadrant.sector) { (acc, sector), _ ->
                    var nextSector = ((sector - 1 + direction) % keyboardData.sectors)
                    if (nextSector < 0)
                        nextSector += keyboardData.sectors
                    nextSector += 1
                    acc + sector to nextSector
                }.first
            }
        }
    }

    private fun getNextPosition(
        quadrant: Quadrant,
        lastPosition: Int,
        keyboardData: KeyboardData
    ): Int {
        val currentSector: Int = quadrant.sector
        val oppositeSector: Int = keyboardData.oppositeDirection(quadrant.sector)
        val currentPart: Int = quadrant.part
        val oppositePart: Int = keyboardData.oppositeDirection(quadrant.part)
        return when (lastPosition) {
            INSIDE_CIRCLE, oppositePart -> {
                currentSector
            }

            oppositeSector -> {
                oppositePart
            }

            currentPart -> {
                oppositeSector
            }

            else -> {
                currentPart
            }
        }
    }
}
