package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import inc.flide.vim8.lib.geometry.toIntOffset
import kotlin.math.min

internal fun Size.isWithinHeight(screenSize: Size) = height > screenSize.minHeight &&
    height < screenSize.maxHeight

internal fun Size.isWithinWidth(screenSize: Size) = width > screenSize.minWidth &&
    width < screenSize.maxWidth

internal inline val Size.minHeight: Float
    get() = min(height, width) * 0.6f
internal inline val Size.maxHeight: Float
    get() = min(height, width) * 0.8f
internal inline val Size.minWidth: Float
    get() = minHeight
internal inline val Size.maxWidth: Float
    get() = maxHeight

fun Size.coerceIn(size: Size): Size = Size(
    width = width.coerceIn(size.minWidth, size.maxWidth),
    height = height.coerceIn(size.minHeight, size.maxHeight)
)

fun IntOffset.coerceIn(size: IntSize, screenSize: IntSize): IntOffset =
    toOffset().coerceIn(size.toSize(), screenSize.toSize()).toIntOffset()

fun Offset.coerceIn(size: Size, screenSize: Size): Offset {
    val (minHeight, maxHeight) = if (screenSize.height > size.height) {
        screenSize.height to size.height
    } else {
        size.height to screenSize.height
    }
    return Offset(
        x.coerceIn(0f, screenSize.width - size.width),
        y.coerceIn(-minHeight, -maxHeight)
    )
}
