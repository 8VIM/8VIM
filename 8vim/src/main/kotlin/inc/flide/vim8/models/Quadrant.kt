package inc.flide.vim8.models

import arrow.optics.optics
import inc.flide.vim8.structures.Constants

@optics
data class Quadrant(val sector: Direction, val part: Direction) {
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

    fun opposite(position: CharacterPosition): Quadrant {
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
}
