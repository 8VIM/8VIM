package inc.flide.vim8.ime.ui.floating.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import arrow.core.getOrElse
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.ui.KeyboardLayoutMode
import inc.flide.vim8.ime.ui.floating.ActiveState
import inc.flide.vim8.ime.ui.floating.CoroutineActiveState
import inc.flide.vim8.ime.ui.floating.CornerPosition
import inc.flide.vim8.ime.ui.floating.boundRectIntoScreen
import inc.flide.vim8.ime.ui.floating.coerceIn
import inc.flide.vim8.ime.ui.floating.minHeight
import inc.flide.vim8.ime.ui.floating.minWidth
import inc.flide.vim8.ime.ui.floating.toCornerPosition
import inc.flide.vim8.lib.android.offset
import inc.flide.vim8.lib.compose.DisposableLifecycleEffect
import inc.flide.vim8.lib.geometry.px2dp

@Composable
fun Floating(content: @Composable FloatingScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenSize = Size(screenWidth, screenHeight)

    val floatingScope = remember { FloatingScope(screenSize) }
        .also { it.activeState = CoroutineActiveState.default() }

    DisposableEffect(Unit) {
        onDispose { floatingScope.stop() }
    }

    DisposableLifecycleEffect(
        onPause = { floatingScope.stop() }
    )

    content(floatingScope)
}

class FloatingScope(val screenSize: Size) {
    private val prefs by appPreferenceModel()
    private val layoutModePref = prefs.keyboard.layoutMode
    lateinit var activeState: ActiveState
    var popupSize by mutableStateOf(Size.Zero)
    val canBeResized: State<Boolean>
        @Composable
        get() = activeState.isActive.collectAsState()

    private val defaultRect = Rect(
        offset = Offset(
            screenSize.width - screenSize.minWidth,
            -screenSize.height + screenSize.minHeight * 1.1f
        ),
        size = Size(screenSize.minWidth, screenSize.minHeight)
    )

    var layoutRect by mutableStateOf(
        layoutModePref
            .floatingRect.get().let { floatingRect ->
                floatingRect
                    .boundRectIntoScreen(screenSize)
                    .getOrElse { defaultRect }
                    .also { rect ->
                        layoutModePref.floatingRect.set(rect)
                    }
            }
    )

    private fun computeMoveOffset(rect: Rect, delta: Offset): Rect {
        return Rect(
            offset = Offset(
                x = (rect.left + delta.x),
                y = (rect.top + delta.y)
            )
                .coerceIn(popupSize, screenSize),
            size = rect.size
        )
    }

    private fun computeResizeRect(cornerPosition: CornerPosition, rect: Rect, delta: Offset): Rect {
        val newSize = cornerPosition.computeSize(
            rect.size,
            screenSize,
            delta
        )

        val newOffset = cornerPosition.computeOffset(
            rect.offset,
            newSize,
            screenSize,
            delta
        )
        return Rect(newOffset, newSize)
    }

    private fun updateLayoutRect() {
        if (layoutRect.top == -popupSize.height) {
            layoutModePref.mode.set(KeyboardLayoutMode.EMBEDDED)
        } else {
            layoutModePref.floatingRect.set(layoutRect)
        }
    }

    fun reset() {
        layoutRect = defaultRect
        layoutModePref.floatingRect.set(layoutRect)
    }

    @Composable
    fun Modifier.resizable(canBeResized: () -> Boolean): Modifier = this.pointerInput(Unit) {
        awaitEachGesture {
            val down =
                awaitFirstDown(pass = PointerEventPass.Initial)
            val direction = Offset(
                down.position.x.px2dp(),
                down.position.y.px2dp()
            )
                .toCornerPosition(popupSize, imePadding.value) ?: return@awaitEachGesture
            do {
                activeState.stop()
                val event =
                    awaitPointerEvent(PointerEventPass.Initial)
                val positionChange = event.changes[0].positionChange()
                val delta = Offset(positionChange.x.px2dp(), positionChange.y.px2dp())
                layoutRect = if (canBeResized()) {
                    computeResizeRect(direction, layoutRect, delta)
                } else {
                    computeMoveOffset(layoutRect, delta)
                }
            } while (event.type == PointerEventType.Move && event.changes.any {
                    it.id == down.id && it.pressed
                }
            )
            if (canBeResized()) {
                activeState.start()
            }
            updateLayoutRect()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Modifier.movable(): Modifier = this.draggable2D(
        state = rememberDraggable2DState {
            if (it == Offset.Zero) return@rememberDraggable2DState
            activeState.stop()
            layoutRect = computeMoveOffset(layoutRect, Offset(it.x.px2dp(), it.y.px2dp()))
        },
        startDragImmediately = true,
        onDragStarted = { activeState.start() },
        onDragStopped = {
            activeState.start()
            updateLayoutRect()
        },
        enabled = true
    )

    fun stop() {
        activeState.stop()
    }
}
