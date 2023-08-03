package inc.flide.vim8.models

import arrow.optics.optics
import inc.flide.vim8.structures.Constants
import java.util.Objects

@optics
data class Quadrant(@JvmField val sector: Direction, @JvmField val part: Direction) {
    companion object

    fun characterIndexInString(characterPosition: CharacterPosition): Int {
        val index = when (sector) {
            Direction.RIGHT -> if (part === Direction.BOTTOM) 0 else 7
            Direction.TOP -> if (part === Direction.LEFT) 5 else 6
            Direction.LEFT -> if (part === Direction.BOTTOM) 3 else 4
            Direction.BOTTOM -> if (part === Direction.RIGHT) 1 else 2
        }
        val base = index / 2 * (Constants.NUMBER_OF_SECTORS * 2)
        val delta = index % 2
        return base + characterPosition.ordinal * 2 + delta
    }

    fun getOppositeQuadrant(position: CharacterPosition): Quadrant {
        return when (position) {
            CharacterPosition.FIRST -> {
                Quadrant(sector, part.opposite())
            }

            CharacterPosition.SECOND -> {
                Quadrant(part, sector)
            }

            CharacterPosition.THIRD -> {
                Quadrant(sector.opposite(), part)
            }

            else -> {
                Quadrant(part.opposite(), sector.opposite())
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val quadrant = other as Quadrant
        return sector === quadrant.sector && part === quadrant.part
    }

    override fun hashCode(): Int {
        return Objects.hash(sector, part)
    }
}