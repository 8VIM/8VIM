package inc.flide.vim8.views

import android.content.Context
import android.graphics.*
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import inc.flide.vim8.R
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper
import inc.flide.vim8.preferences.SharedPreferenceHelper

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