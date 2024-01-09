package inc.flide.vim8.ime.keyboard.text

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.toSize
import inc.flide.vim8.ime.input.InputEventDispatcher
import inc.flide.vim8.ime.keyboard.LocalKeyboardHeight
import inc.flide.vim8.ime.keyboard.text.gestures.SwipeGesture
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.keyboardManager
import inc.flide.vim8.lib.Pointer
import inc.flide.vim8.lib.PointerMap
import inc.flide.vim8.lib.compose.DisposableLifecycleEffect
import inc.flide.vim8.lib.toIntOffset
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.isActive

private val hideKeyboardAction = CustomKeycode.HIDE_KEYBOARD.toKeyboardAction()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardLayout(keyboard: Keyboard): Unit = with(LocalDensity.current) {
    val context = LocalContext.current
    val keyboardUiHeight = LocalKeyboardHeight.current
    val touchEventChannel = remember { Channel<MotionEvent>(64) }
    val controller = remember { KeyboardController(context) }.also { it.keyboard = keyboard }

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardUiHeight)
            .onGloballyPositioned { coords ->
                controller.size = coords.size.toSize()
            }
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_MOVE,
                    MotionEvent.ACTION_POINTER_UP,
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
                return@pointerInteropFilter false
            }
    ) {
        val desiredKey = remember { Key(action = KeyboardAction.UNSPECIFIED) }
        val keyboardWidth = constraints.maxWidth.toFloat()
        val keyboardHeight = constraints.maxHeight.toFloat()
        desiredKey.touchBounds.apply {
            width = keyboardWidth / 10f
            height =
                (keyboardUiHeight / keyboard.rowCount).coerceAtMost(keyboardUiHeight * 1.12f).toPx()
        }
        desiredKey.visibleBounds.applyFrom(desiredKey.touchBounds).deflateBy(2.0f, 5.0f)
        keyboard.layout(keyboardWidth, keyboardHeight, desiredKey)
        for (key in keyboard.keys()) {
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
private fun KeyButton(key: Key) = with(LocalDensity.current) {
    val size = key.visibleBounds.size.toDpSize()
    Box(
        modifier = Modifier
            .requiredSize(size)
            .absoluteOffset { key.visibleBounds.topLeft.toIntOffset() },
    ) {
        val modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = (key.visibleBounds.width / 12f).toDp())
            .align(Alignment.Center)
        if (key.drawableId != null) {
            Image(
                painter = painterResource(key.drawableId),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
        } else {
            Text(
                modifier = modifier,
                text = key.action.text,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private class KeyboardController(context: Context) : SwipeGesture.Listener {
    private val keyboardManager by context.keyboardManager()

    private val inputEventDispatcher get() = keyboardManager.inputEventDispatcher
    private val pointerMap: PointerMap<TouchPointer> = PointerMap { TouchPointer() }
    private val swipeGestureDetector = SwipeGesture.Detector(this)

    lateinit var keyboard: Keyboard
    var size = Size.Zero

    fun onTouchEventInternal(event: MotionEvent) {
        swipeGestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val pointer = pointerMap.add(pointerId, pointerIndex)
                if (pointer != null) {
                    swipeGestureDetector.onTouchDown(event, pointer)
                    onTouchDownInternal(event, pointer)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val oldPointer = pointerMap.findById(pointerId)
                if (oldPointer != null) {
                    swipeGestureDetector.onTouchCancel(oldPointer)
                    onTouchCancelInternal(oldPointer)
                    pointerMap.removeById(oldPointer.id)
                }

                for (pointer in pointerMap) {
                    val activeKey = pointer.activeKey
                    if (activeKey != null) {
                        swipeGestureDetector.onTouchCancel(pointer)
                        onTouchUpInternal(pointer)
                    }
                }

                val pointer = pointerMap.add(pointerId, pointerIndex)
                if (pointer != null) {
                    swipeGestureDetector.onTouchDown(event, pointer)
                    onTouchDownInternal(event, pointer)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (pointerIndex in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(pointerIndex)
                    val pointer = pointerMap.findById(pointerId)
                    if (pointer != null) {
                        pointer.index = pointerIndex
                        if (swipeGestureDetector.onTouchMove(
                                event,
                                pointer
                            ) || pointer.hasTriggeredGestureMove
                        ) {
                            pointer.hasTriggeredGestureMove = true
                        } else {
                            onTouchMoveInternal(event, pointer)
                        }
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val pointer = pointerMap.findById(pointerId)
                if (pointer != null) {
                    pointer.index = pointerIndex
                    if (swipeGestureDetector.onTouchUp(
                            event,
                            pointer,
                            size
                        ) || pointer.hasTriggeredGestureMove
                    ) {
                        onTouchCancelInternal(pointer)
                    } else {
                        onTouchUpInternal(pointer)
                    }
                    pointerMap.removeById(pointer.id)
                }
            }

            MotionEvent.ACTION_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                for (pointer in pointerMap) {
                    if (pointer.id == pointerId) {
                        pointer.index = pointerIndex
                        if (swipeGestureDetector.onTouchUp(
                                event,
                                pointer, size
                            ) || pointer.hasTriggeredGestureMove
                        ) {
                            onTouchCancelInternal(pointer)
                        } else {
                            onTouchUpInternal(pointer)
                        }
                    } else {
                        swipeGestureDetector.onTouchCancel(pointer)
                        onTouchCancelInternal(pointer)
                    }
                }
                pointerMap.clear()
            }

            MotionEvent.ACTION_CANCEL -> {
                for (pointer in pointerMap) {
                    swipeGestureDetector.onTouchCancel(pointer)
                    onTouchCancelInternal(pointer)
                }
                pointerMap.clear()
            }
        }
    }

    private fun onTouchMoveInternal(event: MotionEvent, pointer: TouchPointer) {
        val initialKey = pointer.initialKey
        val activeKey = pointer.activeKey
        if (initialKey != null && activeKey != null) {
            if ((event.getX(pointer.index) < activeKey.visibleBounds.left - 0.1f * activeKey.visibleBounds.width)
                || (event.getX(pointer.index) > activeKey.visibleBounds.right + 0.1f * activeKey.visibleBounds.width)
                || (event.getY(pointer.index) < activeKey.visibleBounds.top - 0.35f * activeKey.visibleBounds.height)
                || (event.getY(pointer.index) > activeKey.visibleBounds.bottom + 0.35f * activeKey.visibleBounds.height)
            ) {
                onTouchCancelInternal(pointer)
                onTouchDownInternal(event, pointer)
            }
        }
    }

    private fun onTouchUpInternal(pointer: TouchPointer) {
        pointer.pressedKeyInfo?.cancelJobs()
        pointer.pressedKeyInfo = null

        val initialKey = pointer.initialKey
        val activeKey = pointer.activeKey
        if (initialKey != null && activeKey != null) {
            if (!pointer.hasTriggeredGestureMove) {
                inputEventDispatcher.sendUp(activeKey.action)
            }
            pointer.activeKey = null
        }
        pointer.hasTriggeredGestureMove = false
    }

    private fun onTouchDownInternal(event: MotionEvent, pointer: TouchPointer) {
        val key = keyboard.getKeyForPos(event.getX(pointer.index), event.getY(pointer.index))
        if (key != null) {
            pointer.pressedKeyInfo = inputEventDispatcher.sendDown(key.action)
            if (pointer.initialKey == null) {
                pointer.initialKey = key
            }
            pointer.activeKey = key
        } else {
            pointer.activeKey = null
        }
    }

    private fun onTouchCancelInternal(pointer: TouchPointer) {
        pointer.pressedKeyInfo?.cancelJobs()
        pointer.pressedKeyInfo = null

        val activeKey = pointer.activeKey
        if (activeKey != null) {
            pointer.activeKey = null
        }
        pointer.hasTriggeredGestureMove = false
    }

    private class TouchPointer : Pointer() {
        var initialKey: Key? = null
        var activeKey: Key? = null
        var hasTriggeredGestureMove: Boolean = false
        var pressedKeyInfo: InputEventDispatcher.PressedKeyInfo? = null

        override fun reset() {
            super.reset()
            initialKey = null
            activeKey = null
            hasTriggeredGestureMove = false
            pressedKeyInfo = null
        }
    }

    override fun onSwipe(direction: SwipeGesture.Direction): Boolean {
        if (direction == SwipeGesture.Direction.DOWN) {
            inputEventDispatcher.sendDownUp(hideKeyboardAction)
        }
        return true
    }
}