package inc.flide.vim8.views

import android.content.Context
import android.inputmethodservice.Keyboard
import android.util.AttributeSet
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener
import inc.flide.vim8.preferences.SharedPreferenceHelper

class SelectionKeypadView : ButtonKeypadView {
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
        val resources = resources
        val foregroundColor: Int = SharedPreferenceHelper.Companion.getInstance(getContext()).getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg))
        val mainInputMethodService = context as MainInputMethodService
        val keyboard = Keyboard(context, R.layout.selection_keypad_view)
        val keys = keyboard.keys
        for (key in keys) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate()
                key.icon.setTint(foregroundColor)
                key.icon.alpha = 255
            }
        }
        val actionListener = ButtonKeypadActionListener(mainInputMethodService, this)
        this.onKeyboardActionListener = actionListener
    }
}