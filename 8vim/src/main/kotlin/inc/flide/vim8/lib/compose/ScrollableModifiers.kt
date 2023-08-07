package inc.flide.vim8.lib.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val defaultScrollbarSize = 4.dp

private val scrollbarAnimationEasing = CubicBezierEasing(1f, 0f, 0.82f, -0.13f)

fun Modifier.verticalScroll(
    state: ScrollState? = null,
    showScrollbar: Boolean = true,
    scrollbarWidth: Dp = defaultScrollbarSize,
) = composed {
    val scrollState = state ?: rememberScrollState()
    if (showScrollbar) {
        verticalScroll(scrollState)
            .scrollbar(state = scrollState, scrollbarSize = scrollbarWidth, isVertical = true)
    } else {
        verticalScroll(scrollState)
    }
}

fun Modifier.scrollbar(
    state: ScrollState,
    scrollbarSize: Dp = defaultScrollbarSize,
    isVertical: Boolean,
): Modifier = composed {
    var isInitial by remember { mutableStateOf(true) }
    val targetAlpha = if (state.isScrollInProgress || isInitial) 1f else 0f
    val duration = if (state.isScrollInProgress || isInitial) 0 else 950
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration, easing = scrollbarAnimationEasing),
        label = "",
    )
    val scrollbarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)

    LaunchedEffect(Unit) {
        delay(1850)
        isInitial = false
    }

    drawWithContent {
        drawContent()
        val needDrawScrollbar = state.isScrollInProgress || isInitial || alpha > 0f
        if (needDrawScrollbar && state.maxValue > 0) {
            val scrollValue = state.value.toFloat()
            val scrollMax = state.maxValue.toFloat()

            val scrollbarWidth: Float
            val scrollbarHeight: Float
            val scrollbarOffsetX: Float
            val scrollbarOffsetY: Float

            if (isVertical) {
                val containerHeight = size.height - scrollMax
                scrollbarWidth = scrollbarSize.toPx()
                scrollbarHeight = containerHeight * (1f - scrollMax / size.height)
                scrollbarOffsetX = size.width - scrollbarWidth
                scrollbarOffsetY =
                    state.value + (containerHeight - scrollbarHeight) * (scrollValue / scrollMax)
            } else {
                val containerWidth = size.width - scrollMax
                scrollbarWidth = containerWidth * (1f - scrollMax / size.width)
                scrollbarHeight = scrollbarSize.toPx()
                scrollbarOffsetX =
                    state.value + (containerWidth - scrollbarWidth) * (scrollValue / scrollMax)
                scrollbarOffsetY = size.height - scrollbarHeight
            }

            drawRect(
                color = scrollbarColor,
                topLeft = Offset(scrollbarOffsetX, scrollbarOffsetY),
                size = Size(scrollbarWidth, scrollbarHeight),
                alpha = alpha,
            )
        }
    }
}
