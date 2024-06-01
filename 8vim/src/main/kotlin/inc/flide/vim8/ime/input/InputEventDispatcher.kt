package inc.flide.vim8.ime.input

import android.os.SystemClock
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.lib.kotlin.guardedByLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InputEventDispatcher {
    companion object {
        val KeyRepeatTimeout = 400L
        val KeyRepeatDelay = 50L
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val pressedKeys = guardedByLock { mutableMapOf<KeyboardAction, PressedKeyInfo>() }
    private var lastKeyEventDown: EventData = EventData(0L, KeyboardAction.UNSPECIFIED)
    private var lastKeyEventUp: EventData = EventData(0L, KeyboardAction.UNSPECIFIED)

    var keyEventReceiver: InputKeyEventReceiver? = null

    fun sendDown(keyboardAction: KeyboardAction) = runBlocking {
        val eventTime = SystemClock.uptimeMillis()
        val result = pressedKeys.withLock { pressedKeys ->
            if (pressedKeys.containsKey(keyboardAction)) return@withLock null
            val pressedKeyInfo = PressedKeyInfo(eventTime).also { pressedKeyInfo ->
                pressedKeyInfo.job = scope.launch {
                    delay(KeyRepeatTimeout)
                    while (isActive) {
                        keyEventReceiver?.onInputKeyDown(keyboardAction, true)
                        pressedKeyInfo.blockUp = true
                        delay(KeyRepeatDelay)
                    }
                }
            }
            pressedKeys[keyboardAction] = pressedKeyInfo
            return@withLock pressedKeyInfo
        }
        if (result != null) {
            keyEventReceiver?.onInputKeyDown(keyboardAction, false)
            lastKeyEventDown = EventData(eventTime, keyboardAction)
        }
        result
    }

    fun sendUp(keyboardAction: KeyboardAction) = runBlocking {
        val result = pressedKeys.withLock { pressedKeys ->
            if (pressedKeys.containsKey(keyboardAction)) {
                pressedKeys.remove(keyboardAction)?.also { it.cancelJobs() }
                return@withLock true
            }
            return@withLock false
        }
        if (result) {
            keyEventReceiver?.onInputKeyUp(keyboardAction, false)
            lastKeyEventUp = EventData(SystemClock.uptimeMillis(), keyboardAction)
        }
    }

    fun sendDownUp(keyboardAction: KeyboardAction, repeat: Boolean = false) = runBlocking {
        pressedKeys.withLock { pressedKeys ->
            pressedKeys.remove(keyboardAction)?.also { it.cancelJobs() }
        }
        val eventData = EventData(SystemClock.uptimeMillis(), keyboardAction)
        keyEventReceiver?.onInputKeyDown(keyboardAction, repeat)
        lastKeyEventDown = eventData
        keyEventReceiver?.onInputKeyUp(keyboardAction, repeat)
        lastKeyEventUp = eventData
    }

    fun sendCancel(keyboardAction: KeyboardAction) = runBlocking {
        pressedKeys.withLock { pressedKeys ->
            if (pressedKeys.containsKey(keyboardAction)) {
                pressedKeys.remove(keyboardAction)?.also { it.cancelJobs() }
            }
        }
    }

    data class PressedKeyInfo(
        val eventTimeDown: Long,
        var job: Job? = null,
        var blockUp: Boolean = false
    ) {
        fun cancelJobs() {
            job?.cancel()
        }
    }

    data class EventData(
        val time: Long,
        val keyboardAction: KeyboardAction
    )
}

interface InputKeyEventReceiver {
    fun onInputKeyDown(keyboardAction: KeyboardAction, repeat: Boolean)
    fun onInputKeyUp(keyboardAction: KeyboardAction, repeat: Boolean)
}
