package inc.flide.vim8.keyboardHelpers

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.coroutineScope
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope

open class LifecycleInputMethodService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner
{
    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val store by lazy { ViewModelStore() }
    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }

    val uiScope: CoroutineScope
        get() = lifecycle.coroutineScope

    final override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    final override fun getViewModelStore(): ViewModelStore {
        return store
    }

    final override fun getSavedStateRegistry(): SavedStateRegistry {
        return savedStateRegistryController.savedStateRegistry
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    override fun onCreateInputView(): View? {
        val decorView = window!!.window!!.decorView
        ViewTreeLifecycleOwner.set(decorView, this)
        ViewTreeViewModelStoreOwner.set(decorView, this)
        ViewTreeSavedStateRegistryOwner.set(decorView, this)
        return null
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
