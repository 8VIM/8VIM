package inc.flide.vim8.keyboardHelpers

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
import android.content.res.Resources
import android.net.Uri
import androidx.preference.PreferenceManager
import java.io.InputStream
import java.lang.Exception

object InputMethodServiceHelper {
    fun initializeKeyboardActionMap(resources: Resources?, context: Context?): KeyboardData? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useCustomSelectedKeyboardLayout = sharedPreferences.getBoolean(
                context.getString(R.string.pref_use_custom_selected_keyboard_layout),
                false)
        if (useCustomSelectedKeyboardLayout) {
            val customKeyboardLayoutString = sharedPreferences.getString(
                    context.getString(R.string.pref_selected_custom_keyboard_layout_uri),
                    null)
            if (customKeyboardLayoutString != null && !customKeyboardLayoutString.isEmpty()) {
                val customKeyboardLayout = Uri.parse(customKeyboardLayoutString)
                return initializeKeyboardActionMapForCustomLayout(resources, context, customKeyboardLayout)
            }
        }
        val mainKeyboardData = getLayoutIndependentKeyboardData(resources)
        val languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context)
        addToKeyboardActionsMapUsingResourceId(
                mainKeyboardData,
                resources,
                languageLayoutResourceId)
        return mainKeyboardData
    }

    fun initializeKeyboardActionMapForCustomLayout(resources: Resources?, context: Context?, customLayoutUri: Uri?): KeyboardData? {
        if (customLayoutUri == null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putBoolean(context.getString(R.string.pref_use_custom_selected_keyboard_layout), false)
            sharedPreferencesEditor.apply()
            return initializeKeyboardActionMap(resources, context)
        }
        val mainKeyboardData = getLayoutIndependentKeyboardData(resources)
        addToKeyboardActionsMapUsingUri(
                mainKeyboardData,
                context,
                customLayoutUri)
        return mainKeyboardData
    }

    private fun getLayoutIndependentKeyboardData(resources: Resources?): KeyboardData? {
        val layoutIndependentKeyboardData = KeyboardData()
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.sector_circle_buttons)
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.d_pad_actions)
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.special_core_gestures)
        return layoutIndependentKeyboardData
    }

    private fun loadTheSelectedLanguageLayout(resources: Resources?, context: Context?): Int {
        val currentLanguageLayout: String = SharedPreferenceHelper.Companion.getInstance(context)
                .getString(resources.getString(R.string.pref_selected_keyboard_layout),
                        LayoutFileName().resourceName)
        return resources.getIdentifier(currentLanguageLayout, "raw", context.getPackageName())
    }

    private fun addToKeyboardActionsMapUsingResourceId(keyboardData: KeyboardData?, resources: Resources?, resourceId: Int) {
        try {
            resources.openRawResource(resourceId).use { inputStream -> addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun addToKeyboardActionsMapUsingUri(keyboardData: KeyboardData?, context: Context?, customLayoutUri: Uri?) {
        try {
            context.getContentResolver().openInputStream(customLayoutUri).use { inputStream -> addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun addToKeyboardActionsMapUsingInputStream(keyboardData: KeyboardData?, inputStream: InputStream?) {
        val keyboardDataXmlParser = KeyboardDataXmlParser(inputStream)
        val tempKeyboardData = keyboardDataXmlParser.readKeyboardData()
        if (validateNoConflictingActions(keyboardData.getActionMap(), tempKeyboardData.actionMap)) {
            keyboardData.addAllToActionMap(tempKeyboardData.actionMap)
        }
        if (keyboardData.getLowerCaseCharacters().isEmpty()
                && !tempKeyboardData.lowerCaseCharacters.isEmpty()) {
            keyboardData.setLowerCaseCharacters(tempKeyboardData.lowerCaseCharacters)
        }
        if (keyboardData.getUpperCaseCharacters().isEmpty()
                && !tempKeyboardData.upperCaseCharacters.isEmpty()) {
            keyboardData.setUpperCaseCharacters(tempKeyboardData.upperCaseCharacters)
        }
    }

    private fun validateNoConflictingActions(
            mainKeyboardActionsMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?,
            newKeyboardActionsMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?): Boolean {
        if (mainKeyboardActionsMap == null || mainKeyboardActionsMap.isEmpty()) {
            return true
        }
        for ((key) in newKeyboardActionsMap) {
            if (mainKeyboardActionsMap.containsKey(key)) {
                return false
            }
        }
        return true
    }
}