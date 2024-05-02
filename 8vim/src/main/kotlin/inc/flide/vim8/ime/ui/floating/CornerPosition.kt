package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

enum class CornerPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT;

    fun computeSize(size: Size, screenSize: Size, delta: Offset): Size {
        val offsetX = if (isLeft) -delta.x else delta.x
        val offsetY = if (isTop) -delta.y else delta.y

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
        val offsetX = if (isLeft && size.isWithinWidth(screenSize)) {
            (offset.x + delta.x)
        } else {
            offset.x
        }
        val offsetY = if (isTop && size.isWithinHeight(screenSize)) {
            (offset.y + delta.y)
        } else {
            offset.y
        }
        return Offset(offsetX, offsetY).coerceIn(size, screenSize)
    }
}

inline val CornerPosition.isLeft: Boolean
    get() = this == CornerPosition.TOP_LEFT || this == CornerPosition.BOTTOM_LEFT
inline val CornerPosition.isTop: Boolean
    get() = this == CornerPosition.TOP_LEFT || this == CornerPosition.TOP_RIGHT

fun Offset.toCornerPosition(size: Size, padding: Float): CornerPosition? {
    val isLeftPosition = x <= (padding * 2)
    val isRightPosition = x >= size.width - (padding * 2)
    val isTopPosition = y <= (padding * 2)
    val isBottomPosition = y >= size.height - (padding * 2)
    return when {
        isTopPosition && isLeftPosition -> CornerPosition.TOP_LEFT
        isTopPosition && isRightPosition -> CornerPosition.TOP_RIGHT
        isBottomPosition && isLeftPosition -> CornerPosition.BOTTOM_LEFT
        isBottomPosition && isRightPosition -> CornerPosition.BOTTOM_RIGHT
        else -> null
    }
}
