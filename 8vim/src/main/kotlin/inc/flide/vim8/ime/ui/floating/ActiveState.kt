package inc.flide.vim8.ime.ui.floating

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ActiveState {
    val isActive: StateFlow<Boolean>

    fun start()
    fun stop()
}

class CoroutineActiveState(private val maxDuration: Duration) : ActiveState {
    companion object {
        fun default(): ActiveState = CoroutineActiveState(5.seconds)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var _isActive = MutableStateFlow(false)
    override val isActive = _isActive.asStateFlow()
    private var job: Job? = null

    override fun start() {
        if (_isActive.value && job?.isActive == true) return
        _isActive.value = true
        job?.cancel()
        job = scope.launch {
            delay(maxDuration)
            _isActive.value = false
        }
    }

    override fun stop() {
        job?.cancel()
    }
}
