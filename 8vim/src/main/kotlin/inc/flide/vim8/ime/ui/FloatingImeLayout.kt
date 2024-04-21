package inc.flide.vim8.ime.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import inc.flide.vim8.R
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.ime.ui.floating.composable.Floating
import inc.flide.vim8.ime.ui.floating.composable.MovingBar
import inc.flide.vim8.ime.ui.floating.composable.SnappingBar
import inc.flide.vim8.lib.android.offset
import inc.flide.vim8.lib.geometry.px2dp
import inc.flide.vim8.lib.geometry.toIntOffset

@Composable
fun FloatingImeLayout(content: @Composable () -> Unit) = with(LocalDensity.current) {
    Floating {
        val strokeWidth = 10.dp.toPx()
        val cornerSize = 32.dp.toPx()
        val borderColor by animateColorAsState(
            if (canBeResized.value) MaterialTheme.colorScheme.outline else Color.Transparent,
            label = "outlineColor"
        )
        val canBeResizedValue by canBeResized
        SnappingBar()

        Popup(
            popupPositionProvider = FloatingPositionProvider(
                layoutRect.offset.toIntOffset(),
                LocalDensity.current
            ),
            properties = PopupProperties(
                dismissOnClickOutside = false
            )
        ) {
            Surface(shadowElevation = 1.dp) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            popupSize = Size(
                                width = it.size.width
                                    .toFloat()
                                    .px2dp(),
                                height = it.size.height
                                    .toFloat()
                                    .px2dp()
                            )
                        }
                        .resizable { canBeResizedValue }
                ) {
                    Column(
                        modifier = Modifier
                            .width(layoutRect.width.dp)
                            .drawWithContent {
                                drawContent()
                                if (canBeResizedValue) {
                                    val outline = RoundedCornerShape(16.dp)
                                        .createOutline(size, LayoutDirection.Ltr, this)
                                    clipRect(
                                        cornerSize,
                                        0f,
                                        size.width - cornerSize,
                                        size.height,
                                        clipOp = ClipOp.Difference
                                    ) {
                                        clipRect(
                                            0f,
                                            cornerSize,
                                            size.width,
                                            size.height - cornerSize,
                                            clipOp = ClipOp.Difference
                                        ) {
                                            drawOutline(
                                                outline = outline,
                                                color = borderColor,
                                                style = Stroke(strokeWidth)
                                            )
                                        }
                                    }
                                }
                            }
                            .border(5.dp, borderColor, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)

                    ) {
                        Box(
                            modifier = Modifier
                                .width(layoutRect.width.dp)
                                .height(layoutRect.height.dp)
                                .padding(imePadding)
                        ) {
                            content()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(imePadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween
                        ) {
                            Icon(
                                modifier = Modifier
                                    .clickable(role = Role.Button) { Vim8ImeService.hideKeyboard() }
                                    .padding(horizontal = imePadding),
                                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                                contentDescription = null
                            )
                            MovingBar()
                            Icon(
                                modifier = Modifier
                                    .clickable(role = Role.Button) { reset() }
                                    .padding(horizontal = imePadding),
                                painter = painterResource(R.drawable.ic_reset),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

private class FloatingPositionProvider(
    val offset: IntOffset,
    val density: Density
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = with(density) {
        IntOffset(offset.x.dp.roundToPx(), offset.y.dp.roundToPx())
            .coerceIn(popupContentSize, windowSize)
    }
}
