package inc.flide.vim8.ime.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.ColorUtils
import arrow.core.getOrElse
import arrow.core.getOrNone
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.text.toKeyboardAction
import inc.flide.vim8.ime.keyboard.xpad.Key
import inc.flide.vim8.ime.keyboard.xpad.Keyboard
import inc.flide.vim8.ime.keyboard.xpad.Keyboard.Companion.extraLayerMovementSequences
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.MovementSequences
import inc.flide.vim8.ime.layout.models.MovementSequenceType
import inc.flide.vim8.keyboardManager
import inc.flide.vim8.lib.compose.DisposableLifecycleEffect
import inc.flide.vim8.lib.observeAsNonNullState
import inc.flide.vim8.lib.toIntOffset
import inc.flide.vim8.lib.util.ViewUtils
import inc.flide.vim8.lib.util.geometry.Circle
import inc.flide.vim8.themeManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

private const val XPAD_CIRCLE_RADIUS_FACTOR = 40f
private const val XPAD_CIRCLE_OFFSET_FACTOR = 26
private const val FULL_ROTATION_STEPS = 7
private val ROTATION_MOVEMENT_SEQUENCES = setOf(
    listOf(
        FingerPosition.BOTTOM,
        FingerPosition.LEFT,
        FingerPosition.TOP,
        FingerPosition.RIGHT,
        FingerPosition.BOTTOM,
        FingerPosition.LEFT
    ),
    listOf(
        FingerPosition.BOTTOM,
        FingerPosition.RIGHT,
        FingerPosition.TOP,
        FingerPosition.LEFT,
        FingerPosition.BOTTOM,
        FingerPosition.RIGHT
    ),
    listOf(
        FingerPosition.LEFT,
        FingerPosition.TOP,
        FingerPosition.RIGHT,
        FingerPosition.BOTTOM,
        FingerPosition.LEFT,
        FingerPosition.TOP
    ),
    listOf(
        FingerPosition.LEFT,
        FingerPosition.BOTTOM,
        FingerPosition.RIGHT,
        FingerPosition.TOP,
        FingerPosition.LEFT,
        FingerPosition.BOTTOM
    ),
    listOf(
        FingerPosition.TOP,
        FingerPosition.LEFT,
        FingerPosition.BOTTOM,
        FingerPosition.RIGHT,
        FingerPosition.TOP,
        FingerPosition.LEFT
    ),
    listOf(
        FingerPosition.TOP,
        FingerPosition.RIGHT,
        FingerPosition.BOTTOM,
        FingerPosition.LEFT,
        FingerPosition.TOP,
        FingerPosition.RIGHT
    ),
    listOf(
        FingerPosition.RIGHT,
        FingerPosition.TOP,
        FingerPosition.LEFT,
        FingerPosition.BOTTOM,
        FingerPosition.RIGHT,
        FingerPosition.TOP
    ),
    listOf(
        FingerPosition.RIGHT,
        FingerPosition.BOTTOM,
        FingerPosition.LEFT,
        FingerPosition.TOP,
        FingerPosition.RIGHT,
        FingerPosition.BOTTOM
    )
)
private val fontFamily = FontFamily(
    Font(R.font.sf_ui_display_regular, FontWeight.Normal),
    Font(R.font.sf_ui_display_bold, FontWeight.Bold),
)

val textStyle = TextStyle(
    fontFamily = fontFamily,
    fontSize = 20.sp
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun XpadLayout() = with(LocalDensity.current) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val prefs by appPreferenceModel()
    val keyboardUiHeight = LocalKeyboardHeight.current
    val path = remember { Path() }
    var radius by remember { mutableFloatStateOf(10f) }
    var circleCentre by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()


    val characterHeight = textMeasurer.measure("A", textStyle).size.height.toFloat()
    var lengthOfLineDemarcatingSectors by remember { mutableFloatStateOf(0f) }
    val keyboard = remember { Keyboard(context) }
    var size by remember { mutableStateOf(Size.Zero) }
    val touchEventChannel = remember { Channel<MotionEvent>(64) }

    val controller = remember { KeyboardController(context) }.also { it.keyboard = keyboard }

    val fg = MaterialTheme.colorScheme.onBackground
    val foregroundPaint = Paint().asFrameworkPaint()
    foregroundPaint.descent()

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
        onPause = { resetAllKeys() },
    )

    fun isTabletLandscape(): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && context.resources.configuration.screenHeightDp >= 480

    fun computeComponentPositions() {
        val keyboardHeightPX = (size.height)
        val circlePrefs = prefs.keyboard.circle
        val spRadiusValue = circlePrefs.radiusSizeFactor.get().toFloat()
        val isSidebarOnLeft = prefs.keyboard.sidebar.isOnLeft.get()
        radius = (spRadiusValue / XPAD_CIRCLE_RADIUS_FACTOR * keyboardHeightPX) / 2f
        val offsetX = circlePrefs.xCentreOffset.get() * XPAD_CIRCLE_OFFSET_FACTOR
        val offsetY = circlePrefs.yCentreOffset.get() * XPAD_CIRCLE_OFFSET_FACTOR

        val smallDim = min(size.width / 2 - offsetX, size.height / 2 - abs(offsetY))
        lengthOfLineDemarcatingSectors = hypot(smallDim, smallDim) - radius - characterHeight
        if (isTabletLandscape()) {
            circleCentre = circleCentre.copy(x = lengthOfLineDemarcatingSectors + offsetX)
            if (!isSidebarOnLeft) {
                circleCentre = circleCentre.copy(x = size.width - circleCentre.x)
            }
        } else {
            circleCentre = circleCentre.copy(x = size.width / 2f + offsetX)
        }
        circleCentre = circleCentre.copy(y = keyboardHeightPX / 2f + offsetY)
        controller.circle.centre = PointF(circleCentre.x, circleCentre.y)
        controller.circle.radius = radius
        path.rewind()
        path.moveTo(circleCentre.x + radius, circleCentre.y)
        path.relativeLineTo(lengthOfLineDemarcatingSectors, 0f)
        path.moveTo(circleCentre.x - radius, circleCentre.y)
        path.relativeLineTo(-lengthOfLineDemarcatingSectors, 0f)
        path.moveTo(circleCentre.x, circleCentre.y + radius)
        path.relativeLineTo(0f, lengthOfLineDemarcatingSectors)
        path.moveTo(circleCentre.x, circleCentre.y - radius)
        path.relativeLineTo(0f, -lengthOfLineDemarcatingSectors)
    }

    prefs.keyboard.circle.radiusSizeFactor.observe { computeComponentPositions() }
    prefs.keyboard.circle.xCentreOffset.observe { computeComponentPositions() }
    prefs.keyboard.circle.yCentreOffset.observe { computeComponentPositions() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardUiHeight)
            .onGloballyPositioned { coords ->
                size = coords.size.toSize()
                computeComponentPositions()
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
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardUiHeight)
        ) {
            val drawStyle = Stroke(context.resources.displayMetrics.density * 2)
            keyboard.layout(characterHeight, circleCentre, radius, lengthOfLineDemarcatingSectors)
//            rotate(45f) {
                drawCircle(fg, radius, circleCentre, style = drawStyle)
                drawPath(path, fg, style = drawStyle)
//            }
        }
        for (key in keyboard.keys) {
            KeyButton(key)
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
private fun KeyButton(key: Key) {
    val context = LocalContext.current
    val themeManager by context.themeManager()
    val currentTheme by themeManager.currentTheme.observeAsNonNullState()
    val letterBackgroundColor = Color(
        ColorUtils.blendARGB(
            currentTheme.trailColor.color(),
            Color.White.toArgb(),
            0.3f
        )
    )

    val keyboardManager by context.keyboardManager()
    val activeState = keyboardManager.activeState
    Box(modifier = Modifier
        .wrapContentSize()
        .absoluteOffset { key.position.toIntOffset() }
        .drawWithContent {
            if (key.isSelected) {
                val topLeft = key.position.copy(
                    x = -key.characterHeightWidth / 2,
                    y = -key.characterHeightWidth
                )
                val width = (key.characterHeightWidth / 2) - topLeft.x
                val height = (key.characterHeightWidth / 2) - topLeft.y
                drawRoundRect(
                    color = letterBackgroundColor,
                    topLeft = topLeft,
                    size = Size(width, height),
                    cornerRadius = CornerRadius(25f)
                )
            }
            drawContent()
        }) {
        val text = key.text(activeState.isUppercase)
        val fontWeight = if (key.isSelected) FontWeight.Bold else FontWeight.Normal
        Text(
            text,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = fontWeight,
            style = textStyle
        )
    }
}

private class KeyboardController(context: Context) {
    private val keyboardManager by context.keyboardManager()

    private val inputEventDispatcher get() = keyboardManager.inputEventDispatcher
    lateinit var keyboard: Keyboard
    var circle: Circle = Circle()
    private var currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    private var currentFingerPosition: FingerPosition = FingerPosition.NO_TOUCH
    private var currentKey: Key? = null
    private val movementSequence: MutableList<FingerPosition> = arrayListOf()

    fun onTouchEventInternal(event: MotionEvent) {
        val position = PointF(event.x, event.y)
        val currentPosition = if (circle.isPointInsideCircle(position)) FingerPosition.INSIDE_CIRCLE
        else circle.getSectorOfPoint(position)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetKey()
                movementSequence.clear()
                currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
                keyboard.reset()
                movementSequence.add(currentPosition)
                currentFingerPosition = currentPosition
            }

            MotionEvent.ACTION_MOVE -> {
                val lastKnownFingerPosition = currentFingerPosition
                currentFingerPosition = currentPosition
                val isFingerPositionChanged = lastKnownFingerPosition !== currentFingerPosition
                if (isFingerPositionChanged) {
                    movementSequence.add(currentFingerPosition)
                    keyboard.findLayer(movementSequence)
                    if (isFullRotation) {
                        var start = 2
                        var size = FULL_ROTATION_STEPS - 1
                        if (keyboard.layerLevel !== LayerLevel.FIRST) {
                            MovementSequences.getOrNone(keyboard.layerLevel).onSome {
                                start += it.size
                                size += it.size
                            }
                        }
                        movementSequence.subList(start, size).clear()
                        inputEventDispatcher.sendDownUp(CustomKeycode.SHIFT_TOGGLE.toKeyboardAction())
                    }

                    if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE && keyboard.hasAction(
                            movementSequence
                        )
                    ) {
                        keyboard
                            .action(movementSequence, currentMovementSequenceType)
                            .onSome { inputEventDispatcher.sendDownUp(it) }
                        resetKey()
                        movementSequence.clear()
                        currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT
                        movementSequence.add(currentFingerPosition)
                    } else if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE) {
                        var layerSize = 0
                        val extraLayerMovementSequences =
                            if (keyboard.layerLevel !== LayerLevel.FIRST) {
                                MovementSequences.getOrNone(keyboard.layerLevel)
                                    .onSome {
                                        layerSize = it.size + 1
                                    }.getOrElse { listOf() }
                            } else {
                                listOf()
                            }
                        val defaultLayerCondition = (
                                keyboard.layerLevel == LayerLevel.FIRST &&
                                        movementSequence[0] == FingerPosition.INSIDE_CIRCLE
                                )
                        val extraLayerCondition =
                            keyboard.layerLevel !== LayerLevel.FIRST && movementSequence.size > layerSize
                        if (defaultLayerCondition || extraLayerCondition) {
                            movementSequence.clear()
                            resetKey()
                            currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
                            movementSequence.addAll(extraLayerMovementSequences)
                            movementSequence.add(currentFingerPosition)
                        }
                    } else {
                        resetKey()
                        val modifiedMovementSequence =
                            movementSequence + FingerPosition.INSIDE_CIRCLE
                        keyboard.key(modifiedMovementSequence)?.let {
                            it.isSelected = true
                            currentKey = it
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                resetKey()
                currentFingerPosition = FingerPosition.NO_TOUCH
                movementSequence.add(currentFingerPosition)
                keyboard
                    .action(movementSequence, currentMovementSequenceType)
                    .onSome { inputEventDispatcher.sendDownUp(it) }

                movementSequence.clear()
                currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
            }

            MotionEvent.ACTION_CANCEL -> {
                resetKey()
                currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
            }
        }
    }

    private fun resetKey() {
        currentKey?.isSelected = false
        currentKey = null
    }

    private val isFullRotation: Boolean
        get() {
            var size = FULL_ROTATION_STEPS
            var start = 1
            var layerCondition = movementSequence[0] == FingerPosition.INSIDE_CIRCLE
            if (keyboard.layerLevel !== LayerLevel.FIRST) {
                MovementSequences.getOrNone(keyboard.layerLevel).onSome {
                    size += it.size
                    start += it.size
                    layerCondition = extraLayerMovementSequences.contains(
                        movementSequence.subList(0, it.size)
                    )
                }
            }
            return if (movementSequence.size == size && layerCondition) {
                ROTATION_MOVEMENT_SEQUENCES.contains(
                    movementSequence.subList(start, size)
                )
            } else {
                false
            }
        }
}
