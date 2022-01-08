package inc.flide.vim8.views

import android.content.Context
import android.inputmethodservice.Keyboard
import android.util.AttributeSet
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener
import inc.flide.vim8.preferences.SharedPreferenceHelper

class SymbolKeypadView : ButtonKeypadView {
    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    fun initialize(context: Context) {
        val mainInputMethodService = context as MainInputMethodService
        val keyboard = Keyboard(context, R.layout.symbols_keypad_view)
        setColors(keyboard)
        val actionListener = ButtonKeypadActionListener(mainInputMethodService, this)
        this.onKeyboardActionListener = actionListener
        SharedPreferenceHelper.getInstance(context).addListener(
            object : SharedPreferenceHelper.Listener() {
                override fun onPreferenceChanged() {
                    setColors(keyboard)
                }
            })
    }

    private fun setColors(keyboard: Keyboard) {
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