package inc.flide.vim8.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.SymbolKeyboardActionListener;

public class SymbolKeypadView extends ButtonKeypadView {

    public SymbolKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SymbolKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;
        this.setKeyboard(new Keyboard(context, R.layout.symbols_keypad_view));
        setHapticFeedbackEnabled(true);
        SymbolKeyboardActionListener actionListener = new SymbolKeyboardActionListener(mainInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }
}
