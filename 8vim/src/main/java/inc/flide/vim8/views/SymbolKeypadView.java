package inc.flide.vim8.views;

import android.content.Context;
import com.hijamoya.keyboardview.Keyboard;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ButtonKeypadActionListener;
import inc.flide.vim8.models.CustomKeycode;

public class SymbolKeypadView extends ButtonKeypadView {

    public SymbolKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        super.initialize(context);

        ButtonKeypadActionListener actionListener =
                new ButtonKeypadActionListener(mainInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.symbols_keypad_view;
    }
}
