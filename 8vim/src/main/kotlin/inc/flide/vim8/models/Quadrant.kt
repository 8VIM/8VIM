package inc.flide.vim8.models

import arrow.optics.optics

const val NUMBER_OF_SECTORS = 4

@optics
data class Quadrant(val sector: Int, val part: Int) {
    companion object
}

fun Quadrant.characterIndexInString(
    characterPosition: CharacterPosition,
    keyboardData: KeyboardData
): Int {
    return if ((part - sector + keyboardData.sectors) % keyboardData.sectors == 1)
        keyboardData.layoutPositions * 2 * (part - 1) + characterPosition.ordinal * 2
    else
        keyboardData.layoutPositions * 2 * (sector - 1) + characterPosition.ordinal * 2 + 1
}

fun Quadrant.opposite(position: CharacterPosition, keyboardData: KeyboardData): Quadrant {
    return when (position) {
        CharacterPosition.FIRST -> {
            Quadrant(sector, keyboardData.oppositeDirection(part))
        }

        CharacterPosition.SECOND -> {
            Quadrant(part, sector)
        }

        CharacterPosition.THIRD -> {
            Quadrant(keyboardData.oppositeDirection(sector), part)
        }

        else -> {
            Quadrant(keyboardData.oppositeDirection(part), keyboardData.oppositeDirection(sector))
        }
    }
}
