package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import inc.flide.vim8.ime.ui.coerceIn
import inc.flide.vim8.ime.ui.isWithinHeight
import inc.flide.vim8.ime.ui.isWithinWidth
import inc.flide.vim8.ime.ui.maxHeight
import inc.flide.vim8.ime.ui.maxWidth
import inc.flide.vim8.ime.ui.minHeight
import inc.flide.vim8.ime.ui.minWidth

enum class Direction {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT;

    fun computeSize(size: Size, screenSize: Size, delta: Offset): Size {
        val offsetX = if (this == TOP_LEFT || this == BOTTOM_LEFT) {
            -delta.x
        } else {
            delta.x
        }

        val offsetY = if (this == TOP_LEFT || this == TOP_RIGHT) {
            -delta.y
        } else {
            delta.y
        }
        return Size(
            width = (size.width + offsetX).coerceIn(
                screenSize.minWidth,
                screenSize.maxWidth
            ),
            height = (size.height + offsetY).coerceIn(
                screenSize.minHeight,
                screenSize.maxHeight
            )
        )
    }
    fun computeOffset(offset: Offset, size: Size, screenSize: Size, delta: Offset): Offset {
        val offsetX = if ((this == TOP_LEFT || this == BOTTOM_LEFT) &&
            size.isWithinWidth(screenSize)
        ) {
            (offset.x + delta.x)
        } else {
            offset.x
        }
        val offsetY = if ((this == TOP_LEFT || this == TOP_RIGHT) &&
            size.isWithinHeight(screenSize)
        ) {
            (offset.y + delta.y)
        } else {
            offset.y
        }
        return Offset(offsetX, offsetY).coerceIn(size, screenSize)
    }
}

fun Offset.toDirection(size: Size, padding: Float): Direction? {
    val isLeft = x <= (padding * 2)
    val isRight = x >= size.width - (padding * 2)
    val isTop = y <= (padding * 2)
    val isBottom = y >= size.height - (padding * 2)
    return when {
        isTop && isLeft -> Direction.TOP_LEFT
        isTop && isRight -> Direction.TOP_RIGHT
        isBottom && isLeft -> Direction.BOTTOM_LEFT
        isBottom && isRight -> Direction.BOTTOM_RIGHT
        else -> null
    }
}
