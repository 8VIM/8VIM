package inc.flide.vim8.ime.ui.floating.compose

import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

fun ContentDrawScope.corners(canBeResized: Boolean, borderColor: Color, density: Density) = with(
    density
) {
    val cornerSizePx = (cornerRadius * 2).toPx()
    val stroke = Stroke((borderWith * 2).toPx())
    drawContent()
    if (canBeResized) {
        val outline = roundedCornerShape.createOutline(size, LayoutDirection.Ltr, this)
        clipRect(
            cornerSizePx,
            0f,
            size.width - cornerSizePx,
            size.height,
            clipOp = ClipOp.Difference
        ) {
            clipRect(
                0f,
                cornerSizePx,
                size.width,
                size.height - cornerSizePx,
                clipOp = ClipOp.Difference
            ) {
                drawOutline(
                    outline = outline,
                    color = borderColor,
                    style = stroke
                )
            }
        }
    }
}
