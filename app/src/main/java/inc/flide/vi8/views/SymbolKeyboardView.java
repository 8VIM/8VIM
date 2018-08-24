package inc.flide.vi8.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.R;
import inc.flide.vi8.keyboardActionListners.SymbolKeyboardActionListener;

public class SymbolKeyboardView extends KeyboardView {

    private SymbolKeyboardActionListener actionListener;

    private Keyboard keyboard;

    public SymbolKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SymbolKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;
        keyboard = new Keyboard(context, R.layout.symbols_keyboard_view);
        this.setKeyboard(keyboard);
        setHapticFeedbackEnabled(true);
        actionListener = new SymbolKeyboardActionListener(mainInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }

}
