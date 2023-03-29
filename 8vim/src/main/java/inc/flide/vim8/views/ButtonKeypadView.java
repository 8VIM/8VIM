package inc.flide.vim8.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public abstract class ButtonKeypadView extends KeyboardView {

    private final Paint foregroundPaint = new Paint();

    public ButtonKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ButtonKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    protected void initialize() {
        //this.setOnKeyboardActionListener(new KeyboardActionListener((MainInputMethodService) context, this));
        this.setHapticFeedbackEnabled(true);

        setColors();
        SharedPreferenceHelper.getInstance(getContext()).addListener(this::setColors);
        this.setPreviewEnabled(false);
    }

    private void setColors() {
        Resources resources = getResources();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());

        String bgColorKeyId = resources.getString(R.string.pref_board_bg_color_key);
        int defaultBackgroundColor = resources.getColor(R.color.defaultBoardBg);

        String fgColorKeyId = resources.getString(R.string.pref_board_fg_color_key);
        int defaultForegroundColor = resources.getColor(R.color.defaultBoardFg);

        int backgroundColor = sharedPreferenceHelper.getInt(bgColorKeyId, defaultBackgroundColor);
        int foregroundColor = sharedPreferenceHelper.getInt(fgColorKeyId, defaultForegroundColor);
        this.setBackgroundColor(backgroundColor);

        foregroundPaint.setColor(foregroundColor);
        foregroundPaint.setTextAlign(Paint.Align.CENTER);
        foregroundPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Regular.otf");
        foregroundPaint.setTypeface(font);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Dimension computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                getResources().getConfiguration().orientation);

        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (key.label != null) {
                canvas.drawText(key.label.toString(), (key.x * 2 + key.width) / 2f, (key.y * 2 + key.height) / 2f, this.foregroundPaint);
            }
            if (key.icon != null) {
                int side = key.height;
                if (key.width < key.height) {
                    side = key.width;
                }
                key.icon.setBounds(key.x + (side / 4), key.y + (side / 4), key.x + (side * 3 / 4), key.y + (side * 3 / 4));
                key.icon.draw(canvas);
            }
        }
    }
}
