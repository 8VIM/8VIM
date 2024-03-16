package inc.flide.vim8.ime.keyboard.xpad

import android.view.MotionEvent
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
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
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
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

private const val FONT_SIZE = 16f
private val cornerRadius = CornerRadius(25f)
private val letterSelectedStroke = Stroke(3f)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun XpadLayout() = with(LocalDensity.current) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val prefs by appPreferenceModel()
    val keyboardUiHeight = LocalKeyboardHeight.current
    val textMeasurer = rememberTextMeasurer()
    val activeState by keyboardManager.activeState.collectAsState()

    val characterHeight = textMeasurer.measure("A", textStyle).size.height.toFloat()

    val keyboard = remember { Keyboard(context) }
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
<<<<<<< HEAD
                drawContent()
=======
>>>>>>> master
                if (controller.hasTrail && controller.trailPoints.isNotEmpty()) {
                    controller.drawTrail(this, controller.trailPoints)
                }
                controller.drawSectors(this, fg)
<<<<<<< HEAD
=======
                drawContent()
>>>>>>> master
            }
    ) {
        val showIcons by prefs.keyboard.display.showSectorIcons.observeAsState()
        val showLetters by prefs.keyboard.display.showLettersOnWheel.observeAsState()
        val isSidebarOnLeft by prefs.keyboard.sidebar.isOnLeft.observeAsState()
<<<<<<< HEAD
=======
        val circleAutoResize by prefs.keyboard.circle.autoResize.observeAsState()
        val radiusMinSizeFactor by prefs.keyboard.circle.radiusMinSizeFactor.observeAsState()
>>>>>>> master
        val radiusSizeFactor by prefs.keyboard.circle.radiusSizeFactor.observeAsState()
        val xCentreOffset by prefs.keyboard.circle.xCentreOffset.observeAsState()
        val yCentreOffset by prefs.keyboard.circle.yCentreOffset.observeAsState()
        val keyboardWidth = constraints.maxWidth.toFloat()
        val keyboardHeight = constraints.maxHeight.toFloat()

        keyboard.layout(
            keyboardWidth,
            keyboardHeight,
            isSidebarOnLeft,
<<<<<<< HEAD
            radiusSizeFactor,
=======
            if (circleAutoResize && controller.isReducesCircleSize) {
                radiusMinSizeFactor
            } else {
                radiusSizeFactor
            },
>>>>>>> master
            xCentreOffset,
            yCentreOffset,
            characterHeight
        )

        if (showLetters) {
            for (key in keyboard.keys) {
                KeyButton(key)
            }
        }

        if (showIcons) {
            SectorImage(
                when (activeState.inputShiftState) {
                    InputShiftState.UNSHIFTED -> R.drawable.ic_no_capslock
                    InputShiftState.SHIFTED -> R.drawable.ic_shift_engaged
                    InputShiftState.CAPS_LOCK -> R.drawable.ic_capslock_engaged
                },
                x = keyboard.circle.centre.x.toDp() - iconHalf,
                y = max(keyboard.bounds.top, 0f).toDp() - iconHalf,
                iconSize
            )

            SectorImage(
                R.drawable.numericpad_vd_vector,
                x = max(keyboard.bounds.left, 0f).toDp() - iconHalf,
                y = keyboard.circle.centre.y.toDp() - iconHalf,
                iconSize
            )

            SectorImage(
                R.drawable.ic_keyboard_return,
                x = keyboard.circle.centre.x.toDp() - iconHalf,
                y = min(keyboard.bounds.bottom, keyboardHeight).toDp() - iconHalf,
                iconSize
            )

            SectorImage(
                R.drawable.ic_backspace,
                x = min(keyboard.bounds.right, keyboardWidth).toDp() - iconHalf,
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

    Text(
        modifier = Modifier
            .absoluteOffset(
                key.position.x.toDp(),
                key.position.y.toDp()
            )
            .drawWithContent {
<<<<<<< HEAD
                drawContent()
=======
>>>>>>> master
                if (key.isSelected) {
                    val topLeft = key.position.copy(
                        x = -FONT_SIZE,
                        y = -FONT_SIZE
                    )
                    val size = this.size.copy(
                        this.size.width + FONT_SIZE * 2f,
                        this.size.height + FONT_SIZE * 2f
                    )
                    drawRoundRect(
                        color = key.backgroundColor,
                        topLeft = topLeft,
                        size = size,
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = topLeft,
                        size = size,
                        cornerRadius = cornerRadius,
                        style = letterSelectedStroke
                    )
                }
<<<<<<< HEAD
=======
                drawContent()
>>>>>>> master
            },
        text = text,
        color = if (key.isSelected) Color.Black else MaterialTheme.colorScheme.onBackground,
        style = style
    )
}
