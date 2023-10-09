package inc.flide.vim8.lib.android

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData

interface Vibration {
    fun vibrate()
}

object HapticVibration {
    private val prefs by appPreferenceModel()
    private val singletons = mutableMapOf<String, Vibration>()

    fun rotation(context: Context): Vibration {
        if (singletons[prefs.inputFeedback.hapticRotateEnabled.key] == null) {
            singletons[prefs.inputFeedback.hapticRotateEnabled.key] =
                if (AndroidVersion.ATLEAST_API26_O) {
                    VibrationAndroidO(context, prefs.inputFeedback.hapticRotateEnabled, 50, 150)
                } else {
                    NoVibration()
                }
        }
        return singletons[prefs.inputFeedback.hapticRotateEnabled.key]!!
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class VibrationAndroidO internal constructor(
    context: Context,
    private val pref: PreferenceData<Boolean>,
    duration: Long,
    amplitude: Int
) : Vibration {
    private val vibrator = context.systemServiceOrNull(Vibrator::class)
    private val effect = VibrationEffect.createOneShot(duration, amplitude)

    override fun vibrate() {
        if (pref.get()) {
            vibrator?.vibrate(effect)
        }
    }
}

class NoVibration internal constructor() : Vibration {
    override fun vibrate() {
    }
}
