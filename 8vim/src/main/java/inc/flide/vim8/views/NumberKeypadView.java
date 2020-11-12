package inc.flide.vim8.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.NumberPadKeyboardActionListener;

public class NumberKeypadView extends ButtonKeypadView {

    public NumberKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public NumberKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;

        this.setKeyboard(new Keyboard(context, R.layout.number_keypad_view));

        NumberPadKeyboardActionListener actionListener = new NumberPadKeyboardActionListener(mainInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }
}
