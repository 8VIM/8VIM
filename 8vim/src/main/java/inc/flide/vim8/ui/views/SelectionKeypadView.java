package inc.flide.vim8.ui.views;

import android.content.Context;
import com.hijamoya.keyboardview.Keyboard;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ButtonKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.utils.ColorsHelper;

public class SelectionKeypadView extends ButtonKeypadView {

    public SelectionKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        super.initialize(context);
        Keyboard keyboard = new Keyboard(context, R.layout.selection_keypad_view);
        setColors(keyboard);
        setKeyboard(keyboard);
        ButtonKeypadActionListener actionListener =
                new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
        SharedPreferenceHelper.getInstance(context).addListener(() -> setColors(keyboard),
                context.getString(R.string.pref_board_fg_color_key),
                context.getString(R.string.pref_board_bg_color_key),
                context.getString(R.string.pref_color_mode_key));
    }

    private void setColors(Keyboard keyboard) {
        int foregroundColor =
                ColorsHelper.getThemeColor(getContext(), R.attr.colorOnSurface,
                        R.string.pref_board_fg_color_key,
                        R.color.defaultBoardFg);

        // Tint icon keys
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate();
                key.icon.setTint(foregroundColor);
                key.icon.setAlpha(Constants.MAX_RGB_COMPONENT_VALUE);
            }
        }

        invalidate();
    }
}
