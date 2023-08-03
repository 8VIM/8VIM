package inc.flide.vim8.models

enum class Direction {
    RIGHT, TOP, LEFT, BOTTOM
}

fun Direction.toFingerPosition(): FingerPosition {
    return when (this) {
        Direction.RIGHT -> {
            FingerPosition.RIGHT
        }

        Direction.TOP -> {
            FingerPosition.TOP
        }

        Direction.LEFT -> {
            FingerPosition.LEFT
        }

        else -> {
            FingerPosition.BOTTOM
        }
    }
}

fun Direction.opposite(): Direction {
    return when (this) {
        Direction.RIGHT -> {
            Direction.LEFT
        }

        Direction.TOP -> {
            Direction.BOTTOM
        }

        Direction.LEFT -> {
            Direction.RIGHT
        }

        else -> {
            Direction.TOP
        }
    }
}