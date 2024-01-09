package inc.flide.vim8.ime.input

import android.os.SystemClock
import android.view.KeyEvent
import android.view.ViewConfiguration
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.lib.kotlin.guardedByLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class InputEventDispatcher private constructor(private val repeatableKeyCodes: IntArray) {
    companion object {
        private val KeyRepeatDelay = ViewConfiguration.getKeyRepeatDelay().toLong()

        fun new(repeatableKeyCodes: IntArray = intArrayOf()) =
            InputEventDispatcher(repeatableKeyCodes.clone())
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val pressedKeys = guardedByLock { mutableMapOf<KeyboardAction, PressedKeyInfo>() }
    private var lastKeyEventDown: EventData = EventData(0L, KeyboardAction.UNSPECIFIED)
    private var lastKeyEventUp: EventData = EventData(0L, KeyboardAction.UNSPECIFIED)

    var keyEventReceiver: InputKeyEventReceiver? = null

    fun sendDown(
        keyboardAction: KeyboardAction,
        onRepeat: () -> Boolean = { true },
    ) = runBlocking {
        val eventTime = SystemClock.uptimeMillis()
        val result = pressedKeys.withLock { pressedKeys ->
            if (pressedKeys.containsKey(keyboardAction)) return@withLock null
            val pressedKeyInfo = PressedKeyInfo(eventTime).also { pressedKeyInfo ->
                pressedKeyInfo.job = scope.launch {
                    if (repeatableKeyCodes.contains(keyboardAction.keyEventCode)) {
                        while (isActive) {
                            val onRepeatResult = withContext(Dispatchers.Main) { onRepeat() }
                            if (onRepeatResult) {
                                keyEventReceiver?.onInputKeyRepeat(keyboardAction)
                                pressedKeyInfo.blockUp = true
                            }
                            delay(KeyRepeatDelay)
                        }
                    }
                }
            }
            pressedKeys[keyboardAction] = pressedKeyInfo
            return@withLock pressedKeyInfo
        }
        if (result != null) {
            keyEventReceiver?.onInputKeyDown(keyboardAction)
            lastKeyEventDown = EventData(eventTime, keyboardAction)
        }
        result
    }

    fun sendUp(keyboardAction: KeyboardAction) = runBlocking {
        val (result, isBlocked) = pressedKeys.withLock { pressedKeys ->
            if (pressedKeys.containsKey(keyboardAction)) {
                val pressedKeyInfo = pressedKeys.remove(keyboardAction)?.also { it.cancelJobs() }
                return@withLock true to (pressedKeyInfo?.blockUp == true)
            }
            return@withLock false to false
        }
        if (result) {
            if (!isBlocked) {
                keyEventReceiver?.onInputKeyUp(keyboardAction)
                lastKeyEventUp = EventData(SystemClock.uptimeMillis(), keyboardAction)
            }
        }
    }

    fun sendDownUp(keyboardAction: KeyboardAction) = runBlocking {
        pressedKeys.withLock { pressedKeys ->
            pressedKeys.remove(keyboardAction)?.also { it.cancelJobs() }
        }
        val eventData = EventData(SystemClock.uptimeMillis(), keyboardAction)
        keyEventReceiver?.onInputKeyDown(keyboardAction)
        lastKeyEventDown = eventData
        keyEventReceiver?.onInputKeyUp(keyboardAction)
        lastKeyEventUp = eventData
    }

    fun close() {
        keyEventReceiver = null
        scope.cancel()
    }

    data class PressedKeyInfo(
        val eventTimeDown: Long,
        var job: Job? = null,
        var blockUp: Boolean = false,
    ) {
        fun cancelJobs() {
            job?.cancel()
        }
    }

    data class EventData(
        val time: Long,
        val keyboardAction: KeyboardAction,
    )
}

interface InputKeyEventReceiver {
    fun onInputKeyDown(keyboardAction: KeyboardAction)
    fun onInputKeyUp(keyboardAction: KeyboardAction)
    fun onInputKeyRepeat(keyboardAction: KeyboardAction)
}
