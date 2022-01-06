package inc.flide.vim8.views

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
import android.util.AttributeSet

class SymbolKeypadView : ButtonKeypadView {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    fun initialize(context: Context?) {
        val mainInputMethodService = context as MainInputMethodService?
        val keyboard = Keyboard(context, R.layout.symbols_keypad_view)
        setColors(keyboard)
        keyboard = keyboard
        val actionListener = ButtonKeypadActionListener(mainInputMethodService, this)
        this.onKeyboardActionListener = actionListener
        SharedPreferenceHelper.Companion.getInstance(context).addListener(SharedPreferenceHelper.Listener { setColors(keyboard) })
    }

    private fun setColors(keyboard: Keyboard?) {
        val resources = resources
        val foregroundColor: Int = SharedPreferenceHelper.Companion.getInstance(context).getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg))

        // Tint icon keys
        for (key in keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate()
                key.icon.setTint(foregroundColor)
                key.icon.alpha = 255
            }
        }
        invalidate()
    }
}