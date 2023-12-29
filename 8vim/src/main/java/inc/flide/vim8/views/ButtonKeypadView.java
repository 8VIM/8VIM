package inc.flide.vim8.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;
import com.hijamoya.keyboardview.Keyboard;
import com.hijamoya.keyboardview.KeyboardView;
import inc.flide.vim8.Vim8ImeService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.ime.KeyboardTheme;
import inc.flide.vim8.ime.layout.models.CustomKeycode;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;

public abstract class ButtonKeypadView extends KeyboardView implements CtrlButtonView, ShiftButtonView {
    private final Paint foregroundPaint = new Paint();
    private final Drawable ctrlDrawable;
    private final Drawable ctrlEngagedDrawable;
    private final Drawable shiftDrawable;
    private final Drawable shiftEngagedDrawable;
    protected Keyboard keyboard;
    protected Vim8ImeService vim8ImeService;
    private Typeface font;
    private KeyboardTheme keyboardTheme;
    private Keyboard.Key ctrlKey;
    private Keyboard.Key shiftKey;


    public ButtonKeypadView(Context context) {
        super(context, null, R.attr.keyboardViewStyle, R.style.KeyboardView);
        vim8ImeService = (Vim8ImeService) context;

        ctrlDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_ctrl);
        ctrlEngagedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_ctrl_engaged);

        shiftDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_no_capslock);
        shiftEngagedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_shift_engaged);

        initialize(context);
    }

    protected void initialize(Context context) {
        setHapticFeedbackEnabled(true);
        keyboardTheme = KeyboardTheme.getInstance();
        font = Typeface.createFromAsset(getContext().getAssets(), "SF-UI-Display-Regular.otf");
        keyboard = new Keyboard(context, getLayoutView());
        setColors();
        keyboardTheme.onChange(this::setColors);
        setPreviewEnabled(false);
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes == null) {
                continue;
            }
            int code = key.codes[0];
            if (code == CustomKeycode.SHIFT_TOGGLE.keyCode) {
                shiftKey = key;
            } else if (code == CustomKeycode.CTRL_TOGGLE.keyCode) {
                ctrlKey = key;
            }
        }
        updateCtrlButton();
        setKeyboard(keyboard);
    }

    public void updateCtrlButton() {
        if (ctrlKey == null) {
            return;
        }

        if (!vim8ImeService.getCtrlState()) {
            ctrlKey.icon = ctrlDrawable;
        } else {
            ctrlKey.icon = ctrlEngagedDrawable;
        }
        ctrlKey.icon = ctrlKey.icon.mutate();
        ctrlKey.icon.setTint(keyboardTheme.getForegroundColor());
        ctrlKey.icon.setAlpha(255);
    }

    public void updateShiftButton() {
        if (shiftKey == null) {
            return;
        }

        if (vim8ImeService.getShiftState() == Vim8ImeService.State.OFF) {
            shiftKey.icon = shiftDrawable;
        } else {
            shiftKey.icon = shiftEngagedDrawable;
        }

        shiftKey.icon = shiftKey.icon.mutate();
        shiftKey.icon.setTint(keyboardTheme.getForegroundColor());
        shiftKey.icon.setAlpha(255);
    }

    protected int getLayoutView() {
        return 0;
    }

    private void setColors() {
        setBackgroundColor(keyboardTheme.getBackgroundColor());

        foregroundPaint.setColor(keyboardTheme.getForegroundColor());
        foregroundPaint.setTextAlign(Paint.Align.CENTER);
        foregroundPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        foregroundPaint.setTypeface(font);

        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate();
                key.icon.setTint(keyboardTheme.getForegroundColor());
                key.icon.setAlpha(255);
            }
        }
        invalidate();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.computeDimension(getResources());
        setMeasuredDimension(computedDimension.width, computedDimension.height);
        super.onMeasure(MeasureSpec.makeMeasureSpec(computedDimension.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.height, MeasureSpec.EXACTLY));
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (key.label != null) {
                canvas.drawText(key.label.toString(), (key.x * 2 + key.width) / 2f, (key.y * 2 + key.height) / 2f,
                        foregroundPaint);
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
