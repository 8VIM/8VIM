package inc.flide.vim8.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import java.util.List;

import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimention;
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper;
import inc.flide.vim8.structures.Constants;

public abstract class ButtonKeypadView extends KeyboardView {


    private final Paint foregroundPaint = new Paint();

    public ButtonKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initilize();
    }

    public ButtonKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initilize();
    }

    protected void initilize() {
        this.setHapticFeedbackEnabled(true);
        this.setBackgroundColor(getResources().getColor(R.color.primaryBackground));
        setupForegroundPaint();
    }

    private void setupForegroundPaint() {
        foregroundPaint.setColor(getResources().getColor(R.color.primaryText));
        foregroundPaint.setTextAlign(Paint.Align.CENTER);
        foregroundPaint.setTextSize(Constants.TEXT_SIZE);
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Regular.otf");
        foregroundPaint.setTypeface(font);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Dimention computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                getResources().getConfiguration().orientation);

        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (key.label != null)
                canvas.drawText(key.label.toString(), (key.x * 2 + key.width) / 2f, (key.y * 2 + key.height) / 2f, this.foregroundPaint);
            if (key.icon != null) {
                int side = key.height;
                if (key.width < key.height) {
                    side = key.width;
                }
                key.icon.setTint(getResources().getColor(R.color.primaryIcon));
                key.icon.setBounds(key.x + (side / 4), key.y + (side / 4), key.x + (side * 3 / 4), key.y + (side * 3 / 4));
                key.icon.draw(canvas);
            }
        }
    }

}
