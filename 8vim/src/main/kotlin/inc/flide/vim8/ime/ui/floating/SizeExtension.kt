package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Size
import kotlin.math.min

internal fun Size.isWithinHeight(screenSize: Size) = height > screenSize.minHeight &&
    height < screenSize.maxHeight

internal fun Size.isWithinWidth(screenSize: Size) = width > screenSize.minWidth &&
    width < screenSize.maxWidth

internal val Size.minHeight: Float
    get() = min(height, width) * 0.6f
internal val Size.maxHeight: Float
    get() = min(height, width) * 0.8f
internal val Size.minWidth: Float
    get() = minHeight
internal val Size.maxWidth: Float
    get() = maxHeight

fun Size.coerceIn(size: Size): Size = Size(
    width = width.coerceIn(size.minWidth, size.maxWidth),
    height = height.coerceIn(size.minHeight, size.maxHeight)
)
