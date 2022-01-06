package inc.flide.vim8.structures

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
import org.apache.commons.lang3.StringUtils
import java.util.*

class LayoutFileName() {
    private var languageCode: String? = "en"
    private var fontCode: String? = "regular"
    private var layoutName: String? = "8pen"
    private var languageName: String? = "English"
    private var isValidLayout = true
    private var resourceName: String?
    private var layoutDisplayName: String?

    constructor(fileName: String?) : this() {
        val nameComponents: Array<String?> = fileName.split("_", 3.toBoolean()).toTypedArray()
        if (nameComponents.size != 3) {
            setLayoutValidityFalse()
            return
        }
        if (ISO_LANGUAGES.contains(nameComponents[0]) && FONT_CODES.contains(nameComponents[1])) {
            resourceName = fileName
            languageCode = nameComponents[0]
            fontCode = nameComponents[1]
            layoutName = nameComponents[2]
            languageName = Locale.forLanguageTag(languageCode).getDisplayName(Locale(languageCode))
            layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
            isValidLayout = true
        } else {
            setLayoutValidityFalse()
        }
    }

    private fun setLayoutValidityFalse() {
        setValidLayout(false)
        languageCode = ""
        fontCode = ""
        layoutName = ""
        languageName = ""
    }

    fun getResourceName(): String? {
        return resourceName
    }

    fun setResourceName(resourceName: String?) {
        this.resourceName = resourceName
    }

    fun getLayoutDisplayName(): String? {
        return layoutDisplayName
    }

    fun setLayoutDisplayName(layoutDisplayName: String?) {
        this.layoutDisplayName = layoutDisplayName
    }

    fun isValidLayout(): Boolean {
        return isValidLayout
    }

    fun setValidLayout(validLayout: Boolean) {
        isValidLayout = validLayout
    }

    fun getLanguageCode(): String? {
        return languageCode
    }

    fun setLanguageCode(languageCode: String?) {
        this.languageCode = languageCode
    }

    fun getFontCode(): String? {
        return fontCode
    }

    fun setFontCode(fontCode: String?) {
        this.fontCode = fontCode
    }

    fun getLayoutName(): String? {
        return layoutName
    }

    fun setLayoutName(layoutName: String?) {
        this.layoutName = layoutName
    }

    fun getLanguageName(): String? {
        return languageName
    }

    fun setLanguageName(languageName: String?) {
        this.languageName = languageName
    }

    companion object {
        private val ISO_LANGUAGES: MutableSet<String?>? = HashSet(Arrays.asList(*Locale.getISOLanguages()))
        private val FONT_CODES: MutableSet<String?>? = HashSet(Arrays.asList(*arrayOf<String?>("regular", "bold", "italic", "underline")))
    }

    init {
        resourceName = languageCode + "_" + fontCode + "_" + layoutName
        layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
    }
}