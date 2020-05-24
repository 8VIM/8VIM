package inc.flide.vi8.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.R;
import inc.flide.vi8.keyboardActionListners.SymbolKeyboardActionListener;
import inc.flide.vi8.structures.Constants;

public class SymbolKeyboardView extends ButtonKeyboardView {

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
