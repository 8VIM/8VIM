package inc.flide.viii.views;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import inc.flide.viii.EightVimInputMethodService;
import inc.flide.viii.R;
import inc.flide.viii.keyboardActionListners.SelectionKeyboardActionListener;

public class SelectionKeyboardView extends KeyboardView {

    private SelectionKeyboardActionListener actionListener;

    private Keyboard keyboard;

    public SelectionKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SelectionKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        EightVimInputMethodService eightVimInputMethodService = (EightVimInputMethodService) context;
        keyboard = new Keyboard(context, R.layout.selection_keyboard_view);
        this.setKeyboard(keyboard);
        setHapticFeedbackEnabled(true);
        actionListener = new SelectionKeyboardActionListener(eightVimInputMethodService, this);
        this.setOnKeyboardActionListener(actionListener);
    }

}
