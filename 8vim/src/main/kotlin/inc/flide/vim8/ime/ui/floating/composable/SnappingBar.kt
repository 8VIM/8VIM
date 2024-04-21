package inc.flide.vim8.ime.ui.floating.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import inc.flide.vim8.Vim8ImeService

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloatingScope.SnappingBar() {
    val outlineColor = MaterialTheme.colorScheme.outline

    AnimatedVisibility(
        visible = layoutRect.top == -popupSize.height,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val width by transition.animateDp(transitionSpec = {
            tween(200, easing = LinearOutSlowInEasing)
        }, label = "width") { state ->
            if (state == EnterExitState.PreEnter) {
                Vim8ImeService.inputFeedbackController()?.performHapticFeedback(0.4)
            }

            if (state == EnterExitState.Visible) screenSize.width.dp else 0.dp
        }
        Popup {
            Box(
                modifier = Modifier
                    .absoluteOffset()
                    .fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(width)
                        .absoluteOffset(),
                    thickness = 5.dp,
                    color = outlineColor
                )
            }
        }
    }
}
