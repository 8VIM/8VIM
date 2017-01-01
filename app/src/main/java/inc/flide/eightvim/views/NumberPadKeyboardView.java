package inc.flide.eightvim.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.keyboardActionListners.NumberPadKeyboardActionListener;

public class NumberPadKeyboardView extends KeyboardView {

    private NumberPadKeyboardActionListener actionListener;

    private Keyboard keyboard;

    public NumberPadKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public NumberPadKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        EightVimInputMethodService eightVimInputMethodService = (EightVimInputMethodService) context;
        keyboard = new Keyboard(context, R.layout.numberpad_keyboard_view);
        this.setKeyboard(keyboard);
        setHapticFeedbackEnabled(true);
        actionListener = new NumberPadKeyboardActionListener(eightVimInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }

}
