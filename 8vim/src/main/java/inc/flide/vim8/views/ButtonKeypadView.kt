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
import inc.flide.vim8.geometry.Circle
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
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
import android.graphics.*
import android.util.AttributeSet

abstract class ButtonKeypadView : KeyboardView {
    private val foregroundPaint: Paint? = Paint()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    protected fun initialize() {
        //this.setOnKeyboardActionListener(new KeyboardActionListener((MainInputMethodService) context, this));
        this.isHapticFeedbackEnabled = true
        setColors()
        SharedPreferenceHelper.Companion.getInstance(context).addListener(SharedPreferenceHelper.Listener { setColors() })
    }

    private fun setColors() {
        val resources = resources
        val sharedPreferenceHelper: SharedPreferenceHelper = SharedPreferenceHelper.Companion.getInstance(context)
        val bgColorKeyId = resources.getString(R.string.pref_board_bg_color_key)
        val defaultBackgroundColor = resources.getColor(R.color.defaultBoardBg)
        val fgColorKeyId = resources.getString(R.string.pref_board_fg_color_key)
        val defaultForegroundColor = resources.getColor(R.color.defaultBoardFg)
        val backgroundColor = sharedPreferenceHelper.getInt(bgColorKeyId, defaultBackgroundColor)
        val foregroundColor = sharedPreferenceHelper.getInt(fgColorKeyId, defaultForegroundColor)
        setBackgroundColor(backgroundColor)
        foregroundPaint.setColor(foregroundColor)
        foregroundPaint.setTextAlign(Paint.Align.CENTER)
        foregroundPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size).toFloat())
        val font = Typeface.createFromAsset(context.assets,
                "SF-UI-Display-Regular.otf")
        foregroundPaint.setTypeface(font)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                resources.configuration.orientation)
        setMeasuredDimension(computedDimension.width, computedDimension.height)
    }

    override fun onDraw(canvas: Canvas?) {
        for (key in keyboard.keys) {
            if (key.label != null) {
                canvas.drawText(key.label.toString(), (key.x * 2 + key.width) / 2f, (key.y * 2 + key.height) / 2f, foregroundPaint)
            }
            if (key.icon != null) {
                var side = key.height
                if (key.width < key.height) {
                    side = key.width
                }
                key.icon.setBounds(key.x + side / 4, key.y + side / 4, key.x + side * 3 / 4, key.y + side * 3 / 4)
                key.icon.draw(canvas)
            }
        }
    }
}