package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import inc.flide.vim8.lib.geometry.toIntOffset

fun IntOffset.coerceIn(size: IntSize, screenSize: IntSize): IntOffset =
    toOffset().coerceIn(size.toSize(), screenSize.toSize()).toIntOffset()

fun Offset.coerceIn(size: Size, screenSize: Size): Offset = if (screenSize.height > size.height) {
    screenSize.height to size.height
} else {
    size.height to screenSize.height
}.let { (minHeight, maxHeight) ->
    Offset(
        x.coerceIn(0f, screenSize.width - size.width),
        y.coerceIn(-minHeight, -maxHeight)
    )
}
