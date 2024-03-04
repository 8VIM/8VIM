package inc.flide.vim8.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Composable
fun <V> LiveData<V>.observeAsNonNullState(
    policy: SnapshotMutationPolicy<V> = structuralEqualityPolicy()
): State<V> = observeAsTransformingState(policy) { it!! }

@Composable
inline fun <V, R> LiveData<V>.observeAsTransformingState(
    policy: SnapshotMutationPolicy<R> = structuralEqualityPolicy(),
    crossinline transform: @DisallowComposableCalls (V?) -> R
): State<R> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(transform(value), policy) }
    DisposableEffect(this, lifecycleOwner) {
        val observer = Observer<V> { state.value = transform(it) }
        observe(lifecycleOwner, observer)
        onDispose { removeObserver(observer) }
    }
    return state
}
