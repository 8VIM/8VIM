package inc.flide.vim8.keyboardactionlisteners;

import android.view.View;
import inc.flide.vim8.MainInputMethodService;

public class SuggestionViewActionListener extends KeypadActionListener {
    public SuggestionViewActionListener(MainInputMethodService mainInputMethodService, View view) {
        super(mainInputMethodService, view);
    }
}
