package inc.flide.eightvim.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.keyboardActionListners.NumberPadKeyboardActionListener;
import inc.flide.eightvim.keyboardActionListners.SymbolKeyboardActionListener;

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
        EightVimInputMethodService eightVimInputMethodService = (EightVimInputMethodService) context;
        keyboard = new Keyboard(context, R.layout.symbols_keyboard_view);
        this.setKeyboard(keyboard);
        setHapticFeedbackEnabled(true);
        actionListener = new SymbolKeyboardActionListener(eightVimInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }

}
