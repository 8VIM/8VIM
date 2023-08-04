package inc.flide.vim8

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.models.appPreferenceModel
import java.lang.ref.WeakReference

private var applicationReference = WeakReference<VIM8Application?>(null)

class VIM8Application : Application() {
    private val prefs by appPreferenceModel()
    override fun onCreate() {
        super.onCreate()
        applicationReference = WeakReference(this)
        prefs.initialize(this)
    }
}

private tailrec fun Context.application(): VIM8Application {
    return when (this) {
        is VIM8Application -> this
        is ContextWrapper -> when {
            this.baseContext != null -> this.baseContext.application()
            else -> applicationReference.get()!!
        }

        else -> tryOrNull { this.applicationContext as VIM8Application }
            ?: applicationReference.get()!!
    }
}

fun Context.appContext() = lazyOf(this.application())
//fun Context.keyboardManager() = this.appContext().keyboardManager
