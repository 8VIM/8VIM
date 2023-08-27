package inc.flide.vim8.lib.android

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <R> tryOrNull(block: () -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (_: Throwable) {
        null
    }
}

abstract class AndroidSettingsHelper {
    abstract fun getString(context: Context, key: String): String?

    abstract fun getUriFor(key: String): Uri?

    private fun observe(context: Context, key: String, observer: SystemSettingsObserver) {
        getUriFor(key)?.let { uri ->
            context.contentResolver.registerContentObserver(uri, false, observer)
            observer.dispatchChange(false, uri)
        }
    }

    private fun removeObserver(context: Context, observer: SystemSettingsObserver) {
        context.contentResolver.unregisterContentObserver(observer)
    }

    @Composable
    fun <R> observeAsState(
        key: String,
        foregroundOnly: Boolean = false,
        transform: (String?) -> R
    ): State<R> {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current.applicationContext
        val state = remember(key) { mutableStateOf(transform(getString(context, key))) }
        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = SystemSettingsObserver(context) {
                state.value = transform(getString(context, key))
            }
            if (foregroundOnly) {
                val eventObserver = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            observe(context, key, observer)
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            removeObserver(context, observer)
                        }

                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(eventObserver)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(eventObserver)
                    removeObserver(context, observer)
                }
            } else {
                observe(context, key, observer)
                onDispose {
                    removeObserver(context, observer)
                }
            }
        }
        return state
    }
}

object AndroidSettings {
    val Secure = object : AndroidSettingsHelper() {
        override fun getString(context: Context, key: String): String? {
            return tryOrNull { Settings.Secure.getString(context.contentResolver, key) }
        }

        override fun getUriFor(key: String): Uri? {
            return tryOrNull { Settings.Secure.getUriFor(key) }
        }
    }
}

fun interface OnSystemSettingsChangedListener {
    fun onChanged()
}

class SystemSettingsObserver(
    context: Context,
    private val listener: OnSystemSettingsChangedListener
) : ContentObserver(Handler(context.mainLooper)) {

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    override fun onChange(selfChange: Boolean) {
        listener.onChanged()
    }
}
