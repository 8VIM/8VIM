package inc.flide.vim8

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import inc.flide.vim8.ime.KeyboardTheme
import inc.flide.vim8.ime.appPreferenceModel
import inc.flide.vim8.ime.layout.Cache
import inc.flide.vim8.theme.ThemeMode
import java.lang.ref.WeakReference

private var applicationReference = WeakReference<VIM8Application?>(null)

class VIM8Application : Application() {
    private val prefs by appPreferenceModel()

    override fun onCreate() {
        super.onCreate()
        applicationReference = WeakReference(this)
        prefs.initialize(this)
        KeyboardTheme.initialize(this)
        Cache.initialize(this)

        when (prefs.theme.mode.get()) {
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }
    }
}
