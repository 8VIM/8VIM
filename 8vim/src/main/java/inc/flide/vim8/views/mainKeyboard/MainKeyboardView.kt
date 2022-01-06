package inc.flide.vim8.views.mainKeyboard

import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Bundle
import inc.flide.vim8.R
import android.view.View.OnTouchListener
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
import android.view.*

class MainKeyboardView : ConstraintLayout {
    private var actionListener: MainKeypadActionListener? = null

    constructor(context: Context?) : super(context) {
        initialize(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    fun initialize(context: Context?) {
        actionListener = MainKeypadActionListener(context as MainInputMethodService?, this)
        setupMainKeyboardView(context)
        setupButtonsOnSideBar()
        setColors()
        isHapticFeedbackEnabled = true
        SharedPreferenceHelper.Companion.getInstance(getContext()).addListener(SharedPreferenceHelper.Listener { setColors() })
    }

    private fun setupMainKeyboardView(context: Context?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val preferredSidebarLeft: Boolean = SharedPreferenceHelper.Companion.getInstance(context)
                .getBoolean(
                        context.getString(R.string.pref_sidebar_left_key),
                        true)
        if (preferredSidebarLeft) {
            inflater.inflate(R.layout.main_keyboard_left_sidebar_view, this, true)
        } else {
            inflater.inflate(R.layout.main_keyboard_right_sidebar_view, this, true)
        }
    }

    private fun setupButtonsOnSideBar() {
        setupSwitchToEmojiKeyboardButton()
        setupSwitchToSelectionKeyboardButton()
        setupTabKey()
        setupGoToSettingsButton()
        setupCtrlKey()
    }

    private fun setImageButtonTint(tintColor: Int, id: Int) {
        val button = findViewById<ImageButton?>(id)
        button.setColorFilter(tintColor)
    }

    private fun setColors() {
        val resources = resources
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.Companion.getInstance(context)
        val backgroundColor = pref.getInt(
                resources.getString(R.string.pref_board_bg_color_key),
                resources.getColor(R.color.defaultBoardBg))
        val tintColor = pref.getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg))
        setBackgroundColor(backgroundColor)
        setImageButtonTint(tintColor, R.id.ctrlButton)
        setImageButtonTint(tintColor, R.id.goToSettingsButton)
        setImageButtonTint(tintColor, R.id.tabButton)
        setImageButtonTint(tintColor, R.id.switchToSelectionKeyboard)
        setImageButtonTint(tintColor, R.id.switchToEmojiKeyboard)
    }

    private fun setupCtrlKey() {
        val ctrlKeyButton = findViewById<ImageButton?>(R.id.ctrlButton)
        ctrlKeyButton.setOnClickListener { view: View? -> actionListener.setModifierFlags(KeyEvent.META_CTRL_MASK) }
    }

    private fun setupGoToSettingsButton() {
        val goToSettingsButton = findViewById<ImageButton?>(R.id.goToSettingsButton)
        goToSettingsButton.setOnClickListener { view: View? ->
            val vim8SettingsIntent = Intent(context, SettingsActivity::class.java)
            vim8SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(vim8SettingsIntent)
        }
    }

    private fun setupTabKey() {
        val tabKeyButton = findViewById<ImageButton?>(R.id.tabButton)
        tabKeyButton.setOnClickListener { view: View? -> actionListener.handleInputKey(KeyEvent.KEYCODE_TAB, 0) }
    }

    private fun setupSwitchToSelectionKeyboardButton() {
        val switchToSelectionKeyboardButton = findViewById<ImageButton?>(R.id.switchToSelectionKeyboard)
        switchToSelectionKeyboardButton.setOnClickListener { view: View? ->
            val switchToSelectionKeyboard = KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.keyCode,
                    0)
            actionListener.handleInputKey(switchToSelectionKeyboard)
        }
    }

    private fun setupSwitchToEmojiKeyboardButton() {
        val switchToEmojiKeyboardButton = findViewById<ImageButton?>(R.id.switchToEmojiKeyboard)
        switchToEmojiKeyboardButton.setOnClickListener { view: View? ->
            val switchToEmojiKeyboard = KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.keyCode,
                    0)
            actionListener.handleInputKey(switchToEmojiKeyboard)
        }
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                resources.configuration.orientation)
        setMeasuredDimension(computedDimension.width, computedDimension.height)
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                        computedDimension.width,
                        MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                        computedDimension.height,
                        MeasureSpec.EXACTLY
                )
        )
    }
}