package inc.flide.vim8.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import java.util.*

class SharedPreferenceHelper private constructor(private val sharedPreferences: SharedPreferences) : OnSharedPreferenceChangeListener {
    abstract class Listener {
        abstract fun onPreferenceChanged()
    }

    private val prefKeys: MutableSet<String> = HashSet()
    private val listeners: MutableList<Listener> = ArrayList()
    fun addListener(note: Listener) {
        listeners.add(note)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (this.prefKeys.contains(s)) {
            for (n in listeners) {
                n.onPreferenceChanged()
            }
        }
    }

    fun getString(preferenceId: String, defaultValue: String = ""): String {
        prefKeys.add(preferenceId)
        val preferencesString: String? = try {
            sharedPreferences.getString(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferencesString ?: defaultValue
    }

    fun getInt(preferenceId: String, defaultValue: Int): Int {
        prefKeys.add(preferenceId)
        val preferenceInt: Int = try {
            sharedPreferences.getInt(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceInt
    }

    fun getBoolean(preferenceId: String, defaultValue: Boolean): Boolean {
        prefKeys.add(preferenceId)
        val preferenceBoolean: Boolean = try {
            sharedPreferences.getBoolean(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceBoolean
    }

    fun getFloat(preferenceId: String, defaultValue: Float): Float {
        prefKeys.add(preferenceId)
        val preferenceFloat: Float = try {
            sharedPreferences.getFloat(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceFloat
    }

    companion object {
        private var singleton: SharedPreferenceHelper? = null
        fun getInstance(context: Context): SharedPreferenceHelper {
            //These two ifs should be probably swapped as it can still return null
            //when singleton is null.
            if (singleton == null) {
                val sp = PreferenceManager.getDefaultSharedPreferences(context)
                singleton = SharedPreferenceHelper(sp)
            }
            return singleton as SharedPreferenceHelper
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}