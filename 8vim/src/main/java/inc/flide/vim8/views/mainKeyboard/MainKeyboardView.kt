package inc.flide.vim8.views.mainKeyboard

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.*
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.structures.KeyboardAction
import inc.flide.vim8.structures.KeyboardActionType
import inc.flide.vim8.ui.SettingsActivity

class MainKeyboardView : ConstraintLayout {
    private lateinit var actionListener: MainKeypadActionListener

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        actionListener = MainKeypadActionListener(this)
        setupMainKeyboardView(context)
        setupButtonsOnSideBar()
        setColors()
        isHapticFeedbackEnabled = true
        SharedPreferenceHelper.getInstance(context).addListener(
            object : SharedPreferenceHelper.Listener() {
                override fun onPreferenceChanged() {
                    setColors()
                }

            })
    }

    private fun setupMainKeyboardView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val preferredSidebarLeft: Boolean = SharedPreferenceHelper.getInstance(context)
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
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.getInstance(context)
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
        ctrlKeyButton.setOnClickListener { MainInputMethodService.setModifierFlags(KeyEvent.META_CTRL_MASK) }
    }

    private fun setupGoToSettingsButton() {
        val goToSettingsButton = findViewById<ImageButton?>(R.id.goToSettingsButton)
        goToSettingsButton.setOnClickListener {
            val vim8SettingsIntent = Intent(context, SettingsActivity::class.java)
            vim8SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(vim8SettingsIntent)
        }
    }

    private fun setupTabKey() {
        val tabKeyButton = findViewById<ImageButton?>(R.id.tabButton)
        tabKeyButton.setOnClickListener { actionListener.handleInputKey(KeyEvent.KEYCODE_TAB, 0) }
    }

    private fun setupSwitchToSelectionKeyboardButton() {
        val switchToSelectionKeyboardButton = findViewById<ImageButton?>(R.id.switchToSelectionKeyboard)
        switchToSelectionKeyboardButton.setOnClickListener {
            val switchToSelectionKeyboard = KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.getKeyCode(),
                    0)
            actionListener.handleInputKey(switchToSelectionKeyboard)
        }
    }

    private fun setupSwitchToEmojiKeyboardButton() {
        val switchToEmojiKeyboardButton = findViewById<ImageButton?>(R.id.switchToEmojiKeyboard)
        switchToEmojiKeyboardButton.setOnClickListener {
            val switchToEmojiKeyboard = KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.getKeyCode(),
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
