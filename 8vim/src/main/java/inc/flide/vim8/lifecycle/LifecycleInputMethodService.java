package inc.flide.vim8.lifecycle;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.lifecycle.ViewTreeViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;
import androidx.savedstate.ViewTreeSavedStateRegistryOwner;

public abstract class LifecycleInputMethodService extends InputMethodService implements LifecycleOwner,
        ViewModelStoreOwner,
        SavedStateRegistryOwner {
    private final LifecycleRegistry lifecycleRegistry;
    private final ViewModelStore store;
    private final SavedStateRegistryController savedStateRegistryController;

    public LifecycleInputMethodService() {
        lifecycleRegistry = new LifecycleRegistry(this);
        store = new ViewModelStore();
        savedStateRegistryController = SavedStateRegistryController.create(this);
    }

    @NonNull
    @Override
    public SavedStateRegistry getSavedStateRegistry() {
        return savedStateRegistryController.getSavedStateRegistry();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return store;
    }

    protected void installViewTreeOwners() {
        View decorView = getWindow().getWindow().getDecorView();
        ViewTreeLifecycleOwner.set(decorView, this);
        ViewTreeViewModelStoreOwner.set(decorView, this);
        ViewTreeSavedStateRegistryOwner.set(decorView, this);
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }
}
