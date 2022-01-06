package inc.flide.vim8.preferences

import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Bundle
import inc.flide.vim8.R
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.content.Intent
import inc.flide.vim8.ui.SettingsActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import inc.flide.vim8.ui.SettingsFragment
import android.widget.Toast
import inc.flide.vim8.ui.AboutUsActivity
import androidx.core.view.GravityCompat
import android.view.inputmethod.InputMethodInfo
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.afollestad.materialdialogs.DialogAction
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Gravity
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences
import android.app.Activity
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import androidx.preference.SeekBarPreference
import inc.flide.vim8.preferences.SharedPreferenceHelper
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
import inc.flide.vim8.R.raw
import inc.flide.vim8.structures.LayoutFileName
import android.graphics.PointF
import inc.flide.vim8.geometry.Circle
import android.graphics.RectF
import android.graphics.PathMeasure
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
import android.graphics.Typeface
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import inc.flide.vim8.structures.FingerPosition
import android.widget.ImageButton
import inc.flide.vim8.structures.KeyboardAction
import inc.flide.vim8.structures.KeyboardActionType
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.Keyboard
import inc.flide.vim8.views.ButtonKeypadView
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener
import inc.flide.vim8.geometry.GeometricUtilities
import kotlin.jvm.JvmOverloads
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import org.xmlpull.v1.XmlPullParser
import kotlin.Throws
import org.xmlpull.v1.XmlPullParserException
import inc.flide.vim8.structures.KeyboardData
import inc.flide.vim8.keyboardHelpers.KeyboardDataXmlParser
import android.util.Xml
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import inc.flide.vim8.keyboardActionListners.KeypadActionListener
import inc.flide.vim8.structures.MovementSequenceType
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import android.os.IBinder
import android.text.TextUtils
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import java.lang.ClassCastException
import java.util.ArrayList
import java.util.HashSet

class SharedPreferenceHelper private constructor(private val sharedPreferences: SharedPreferences?) : OnSharedPreferenceChangeListener {
    interface Listener {
        open fun onPreferenceChanged()
    }

    private val prefKeys: MutableSet<String?>? = HashSet()
    private val listeners: MutableList<Listener?>? = ArrayList()
    fun addListener(note: Listener?) {
        listeners.add(note)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (prefKeys.contains(s)) {
            for (n in listeners) {
                n.onPreferenceChanged()
            }
        }
    }

    fun getString(preferenceId: String?, defaultValue: String?): String? {
        prefKeys.add(preferenceId)
        val preferencesString: String?
        preferencesString = try {
            sharedPreferences.getString(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferencesString
    }

    fun getInt(preferenceId: String?, defaultValue: Int): Int {
        prefKeys.add(preferenceId)
        val preferenceInt: Int
        preferenceInt = try {
            sharedPreferences.getInt(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceInt
    }

    fun getBoolean(preferenceId: String?, defaultValue: Boolean): Boolean {
        prefKeys.add(preferenceId)
        val preferenceBoolean: Boolean
        preferenceBoolean = try {
            sharedPreferences.getBoolean(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceBoolean
    }

    fun getFloat(preferenceId: String?, defaultValue: Float): Float {
        prefKeys.add(preferenceId)
        val preferenceFloat: Float
        preferenceFloat = try {
            sharedPreferences.getFloat(preferenceId, defaultValue)
        } catch (e: ClassCastException) {
            return defaultValue
        }
        return preferenceFloat
    }

    companion object {
        private var singleton: SharedPreferenceHelper? = null
        fun getInstance(context: Context?): SharedPreferenceHelper? {
            //These two ifs should be probably swapped as it can still return null
            //when singleton is null.
            if (context == null) {
                return singleton
            }
            if (singleton == null) {
                val sp = PreferenceManager.getDefaultSharedPreferences(context)
                singleton = SharedPreferenceHelper(sp)
            }
            return singleton
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
}