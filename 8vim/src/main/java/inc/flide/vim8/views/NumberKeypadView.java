package inc.flide.vim8.views;

import android.content.Context;
import android.content.res.Resources;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public class NumberKeypadView extends ButtonKeypadView {

    public NumberKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public NumberKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context) {

        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;

        Keyboard keyboard = new Keyboard(context, R.layout.number_keypad_view);
        setColors(keyboard);
        this.setKeyboard(keyboard);

        ButtonKeypadActionListener actionListener = new ButtonKeypadActionListener(mainInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
        SharedPreferenceHelper.getInstance(getContext()).addListener(() -> setColors(keyboard));
    }

    private void setColors(Keyboard keyboard) {
        Resources resources = getResources();
        int foregroundColor = SharedPreferenceHelper.getInstance(getContext()).getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg));
        // Tint icon keys
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                // TODO: find more info
                key.icon = key.icon.mutate();
                key.icon.setTint(foregroundColor);
                key.icon.setAlpha(255);
            }
        }
        invalidate();
    }
}
