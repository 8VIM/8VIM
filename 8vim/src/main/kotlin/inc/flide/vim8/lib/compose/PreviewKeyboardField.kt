package inc.flide.vim8.lib.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection
import inc.flide.vim8.R
import inc.flide.vim8.utils.InputMethodUtils

fun EnterTransition.Companion.verticalTween(
    duration: Int,
    expandFrom: Alignment.Vertical = Alignment.Bottom,
): EnterTransition {
    return fadeIn(tween(duration)) + expandVertically(tween(duration), expandFrom)
}

fun ExitTransition.Companion.verticalTween(
    duration: Int,
    shrinkTowards: Alignment.Vertical = Alignment.Bottom,
): ExitTransition {
    return fadeOut(tween(duration)) + shrinkVertically(tween(duration), shrinkTowards)
}

private const val AnimationDuration = 200

private val previewEnterTransition = EnterTransition.verticalTween(AnimationDuration)
private val previewExitTransition = ExitTransition.verticalTween(AnimationDuration)

val LocalPreviewFieldController = staticCompositionLocalOf<PreviewFieldController?> { null }

@Composable
fun rememberPreviewFieldController(): PreviewFieldController {
    return remember { PreviewFieldController() }
}

class PreviewFieldController {
    val focusRequester = FocusRequester()
    var isVisible by mutableStateOf(false)
    var text by mutableStateOf(TextFieldValue(""))
}

fun Context.showShortToast(text: String): Toast {
    return Toast.makeText(this, text, Toast.LENGTH_SHORT).also { it.show() }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PreviewKeyboardField(
    controller: PreviewFieldController,
    modifier: Modifier = Modifier,
    hint: String = stringRes(R.string.keyboard_test_edit_text_hint),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = controller.isVisible,
        enter = previewEnterTransition,
        exit = previewExitTransition,
    ) {
        SelectionContainer {
            TextField(
                modifier = modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Back) {
                            focusManager.clearFocus()
                        }
                        false
                    }
                    .focusRequester(controller.focusRequester),
                value = controller.text,
                onValueChange = { controller.text = it },
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.ContentOrLtr),
                placeholder = { Text(text = hint) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            if (!InputMethodUtils.showImePicker(context)) {
                                context.showShortToast("Error: InputMethodManager service not available!")
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_keyboard),
                                contentDescription = null,
                            )
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
