package inc.flide.vim8.datastore.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport

@ExcludeFromJacocoGeneratedReport
@Composable
fun <V : Any> PreferenceData<V>.observeAsState(): State<V> = observeAsState(get())

@ExcludeFromJacocoGeneratedReport
@Composable
fun <V : Any> PreferenceData<V>.observeAsState(initial: V): State<V> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(key) { mutableStateOf(initial) }
    DisposableEffect(this, lifecycleOwner) {
        val observer = PreferenceObserver<V> { newValue -> state.value = newValue }
        observe(lifecycleOwner, observer)
        onDispose { removeObserver(observer) }
    }
    return state
}
