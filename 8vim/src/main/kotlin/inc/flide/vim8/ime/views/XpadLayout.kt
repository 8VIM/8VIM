package inc.flide.vim8.ime.views

import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.xpad.Key
import inc.flide.vim8.ime.keyboard.xpad.Keyboard
import inc.flide.vim8.ime.keyboard.xpad.KeyboardController
import inc.flide.vim8.keyboardManager
import inc.flide.vim8.lib.compose.DisposableLifecycleEffect
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.isActive

private val textStyle = TextStyle(
    fontFamily = FontFamily(Font(R.font.sf_ui_display_regular, FontWeight.Normal)),
    fontSize = 20.sp,
    textAlign = TextAlign.Center
)

private val textStyleBold = TextStyle(
    fontFamily = FontFamily(Font(R.font.sf_ui_display_bold, FontWeight.Normal)),
    fontSize = 20.sp,
    textAlign = TextAlign.Center
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun XpadLayout() = with(LocalDensity.current) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val prefs by appPreferenceModel()
    val keyboardUiHeight = LocalKeyboardHeight.current
    val textMeasurer = rememberTextMeasurer()
    val activeState by keyboardManager.activeState.collectAsState()
    val showIcons by prefs.keyboard.display.showSectorIcons.observeAsState()
    val showLetters by prefs.keyboard.display.showLettersOnWheel.observeAsState()
    val isSidebarOnLeft by prefs.keyboard.sidebar.isOnLeft.observeAsState()
    val radiusSizeFactor by prefs.keyboard.circle.radiusSizeFactor.observeAsState()
    val xCentreOffset by prefs.keyboard.circle.xCentreOffset.observeAsState()
    val yCentreOffset by prefs.keyboard.circle.yCentreOffset.observeAsState()

    val keyboard = remember { Keyboard(context) }
    var size by remember { mutableStateOf(Size.Zero) }
    val touchEventChannel = remember { Channel<MotionEvent>(64) }

    val controller = remember { KeyboardController(context) }.also { it.keyboard = keyboard }
    val fg = MaterialTheme.colorScheme.onBackground
    val iconSize = 25.sp.toDp()
    val iconHalf = iconSize / 2

    fun resetAllKeys() {
        try {
            val event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
            controller.onTouchEventInternal(event)
            event.recycle()
        } catch (e: Throwable) {
            // Ignore
        }
    }

    DisposableEffect(Unit) {
        onDispose { resetAllKeys() }
    }

    DisposableLifecycleEffect(
        onPause = { resetAllKeys() }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardUiHeight)
            .onGloballyPositioned { coords ->
                size = coords.size.toSize()
            }
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE,
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL
                    -> {
                        val clonedEvent = MotionEvent.obtainNoHistory(event)
                        touchEventChannel
                            .trySend(clonedEvent)
                            .onFailure {
                                clonedEvent.recycle()
                            }
                        return@pointerInteropFilter true
                    }
                }
                return@pointerInteropFilter true
            }
            .drawWithContent {
                drawContent()
                if (controller.hasTrail && controller.trailPoints.isNotEmpty()) {
                    controller.drawTrail(this, controller.trailPoints)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardUiHeight)
        ) {
            keyboard.layout(
                size,
                isSidebarOnLeft,
                radiusSizeFactor,
                xCentreOffset,
                yCentreOffset,
                textMeasurer,
                textStyle
            )
            controller.drawSectors(this, fg)
        }

        if (showLetters) {
            for (key in keyboard.keys) {
                KeyButton(key)
            }
        }

        if (showIcons && !controller.bounds.isEmpty) {
            SectorImage(
                when (activeState.inputShiftState) {
                    InputShiftState.UNSHIFTED -> R.drawable.ic_no_capslock
                    InputShiftState.SHIFTED -> R.drawable.ic_shift_engaged
                    InputShiftState.CAPS_LOCK -> R.drawable.ic_capslock_engaged
                },
                x = keyboard.circle.centre.x.toDp() - iconHalf,
                y = max(controller.bounds.top, 0f).toDp() + iconHalf,
                iconSize
            )
            SectorImage(
                R.drawable.numericpad_vd_vector,
                x = max(controller.bounds.left, 0f).toDp() + iconSize,
                y = keyboard.circle.centre.y.toDp() - iconHalf,
                iconSize
            )

            SectorImage(
                R.drawable.ic_keyboard_return,
                x = keyboard.circle.centre.x.toDp() - iconHalf,
                y = min(controller.bounds.bottom, size.height).toDp() - iconSize,
                iconSize
            )

            SectorImage(
                R.drawable.ic_backspace,
                x = min(controller.bounds.right, size.width).toDp() + iconSize,
                y = keyboard.circle.centre.y.toDp() - iconHalf,
                iconSize
            )
        }
    }

    LaunchedEffect(Unit) {
        for (event in touchEventChannel) {
            if (!isActive) break
            controller.onTouchEventInternal(event)
            event.recycle()
        }
    }
}

@Composable
private fun SectorImage(@DrawableRes res: Int, x: Dp, y: Dp, size: Dp) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier
            .absoluteOffset(x, y)
            .requiredSize(size),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        alpha = 0.33f
    )
}

@Composable
private fun KeyButton(key: Key) = with(LocalDensity.current) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val activeState by keyboardManager.activeState.collectAsState()
    val text = key.text(activeState.isUppercase)
    val style = if (key.isSelected) textStyleBold else textStyle
    val fontSize = 16f

    Text(
        modifier = Modifier
            .absoluteOffset(
                key.position.x.toDp(),
                key.position.y.toDp()
            )
            .drawWithContent {
                if (key.isSelected) {
                    val topLeft = key.position.copy(
                        x = -fontSize,
                        y = -fontSize
                    )
                    val size = this.size.copy(
                        this.size.width + fontSize * 2f,
                        this.size.height + fontSize * 2f
                    )
                    drawRoundRect(
                        color = key.backgroundColor,
                        topLeft = topLeft,
                        size = size,
                        cornerRadius = CornerRadius(25f)
                    )
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = topLeft,
                        size = size,
                        cornerRadius = CornerRadius(25f),
                        style = Stroke(3f)
                    )
                }
                drawContent()
            },
        text = text,
        color = if (key.isSelected) Color.Black else MaterialTheme.colorScheme.onBackground,
        style = style
    )
}
