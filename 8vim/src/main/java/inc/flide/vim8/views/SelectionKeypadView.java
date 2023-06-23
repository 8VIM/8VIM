package inc.flide.vim8.views;

import android.content.Context;
import com.hijamoya.keyboardview.Keyboard;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ButtonKeypadActionListener;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.utils.ColorsHelper;
import java.util.List;

public class SelectionKeypadView extends ButtonKeypadView {

    public SelectionKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        int foregroundColor =
                ColorsHelper.getThemeColor(getContext(), R.attr.colorOnBackground,
                        R.string.pref_board_fg_color_key,
                        R.color.defaultBoardFg);

        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;

        Keyboard keyboard = new Keyboard(context, R.layout.selection_keypad_view);
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (Keyboard.Key key : keys) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate();
                key.icon.setTint(foregroundColor);
                key.icon.setAlpha(Constants.MAX_RGB_COMPONENT_VALUE);
            }
        }
        setKeyboard(keyboard);

        ButtonKeypadActionListener actionListener =
                new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
    }
}
