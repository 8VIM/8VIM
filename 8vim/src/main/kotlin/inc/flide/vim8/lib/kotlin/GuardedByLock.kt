package inc.flide.vim8.lib.kotlin

import kotlinx.coroutines.sync.Mutex
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class GuardedByLock<out T : Any>(@PublishedApi internal val wrapped: T) {
    @PublishedApi
    internal val lock = Mutex(locked = false)

    suspend inline fun <R> withLock(owner: Any? = null, action: (T) -> R): R {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        lock.lock(owner)
        try {
            return action(wrapped)
        } finally {
            lock.unlock(owner)
        }
    }
}

inline fun <T : Any> guardedByLock(initializer: () -> T): GuardedByLock<T> {
    contract {
        callsInPlace(initializer, InvocationKind.EXACTLY_ONCE)
    }
    return GuardedByLock(initializer())
}
