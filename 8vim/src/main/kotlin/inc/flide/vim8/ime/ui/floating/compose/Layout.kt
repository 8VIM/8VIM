package inc.flide.vim8.ime.ui.floating.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
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
import inc.flide.vim8.ime.ui.floating.coerceIn
import inc.flide.vim8.lib.android.offset
import inc.flide.vim8.lib.geometry.px2dp
import inc.flide.vim8.lib.geometry.toIntOffset

internal val borderWith = 5.dp
internal val cornerRadius = 16.dp
internal val imePadding = 16.dp
internal val roundedCornerShape = RoundedCornerShape(cornerRadius)

@Composable
fun Layout(content: @Composable () -> Unit) = Floating {
    val borderColor = MaterialTheme.colorScheme.onSurface
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
        Surface(
            shape = roundedCornerShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
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
                            corners(canBeResizedValue, borderColor, this)
                        }
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
                            .padding(horizontal = imePadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween
                    ) {
                        Icon(
                            modifier = Modifier
                                .clickable(role = Role.Button) { Vim8ImeService.hideKeyboard() },

                            painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                            contentDescription = null
                        )
                        MovingBar()
                        Icon(
                            modifier = Modifier.matchRowSize()
                                .clickable(role = Role.Button) { reset() },
                            painter = painterResource(R.drawable.ic_reset),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.matchRowSize(): Modifier {
    return layout { measurable, constraints ->
        if (constraints.maxHeight == Constraints.Infinity) {
            layout(0, 0) {}
        } else {
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
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
