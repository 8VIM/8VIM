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
        ButtonKeypadActionListener actionListener =
                new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.selection_keypad_view;
    }
}
