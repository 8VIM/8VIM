package inc.flide.vim8.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.hijamoya.keyboardview.Keyboard;
import com.hijamoya.keyboardview.KeyboardView;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.ui.Theme;

public abstract class ButtonKeypadView extends KeyboardView {
    private final Paint foregroundPaint = new Paint();
    protected Keyboard keyboard;
    protected MainInputMethodService mainInputMethodService;
    private Typeface font;

    public ButtonKeypadView(Context context) {
        super(context, null, R.attr.keyboardViewStyle, R.style.KeyboardView);
        mainInputMethodService = (MainInputMethodService) context;
        initialize(context);
    }

    protected void initialize(Context context) {
        this.setHapticFeedbackEnabled(true);

        font = Typeface.createFromAsset(getContext().getAssets(), "SF-UI-Display-Regular.otf");
        keyboard = new Keyboard(context, getLayoutView());
        setColors();
        Theme
                .getInstance(getContext())
                .onChange(this::setColors);

        this.setPreviewEnabled(false);
        setKeyboard(keyboard);
    }

    protected int getLayoutView() {
        return 0;
    }

    private void setColors() {
        this.setBackgroundColor(Theme.getBackgroundColor());

        foregroundPaint.setColor(Theme.getForegroundColor());
        foregroundPaint.setTextAlign(Paint.Align.CENTER);
        foregroundPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        foregroundPaint.setTypeface(font);
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate();
                key.icon.setTint(Theme.getForegroundColor());
                key.icon.setAlpha(Constants.MAX_RGB_COMPONENT_VALUE);
            }
        }
        invalidate();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.computeDimension(getResources());
        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());
        super.onMeasure(MeasureSpec.makeMeasureSpec(computedDimension.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.getHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (key.label != null) {
                canvas.drawText(key.label.toString(), (key.x * 2 + key.width) / 2f, (key.y * 2 + key.height) / 2f,
                        this.foregroundPaint);
            }
            if (key.icon != null) {
                int side = key.height;
                if (key.width < key.height) {
                    side = key.width;
                }
                key.icon.setBounds(key.x + (side / 4), key.y + (side / 4), key.x + (side * 3 / 4),
                        key.y + (side * 3 / 4));
                key.icon.draw(canvas);
            }
        }
    }
}
