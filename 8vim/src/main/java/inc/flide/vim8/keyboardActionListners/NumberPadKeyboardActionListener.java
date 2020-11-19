package inc.flide.vim8.keyboardActionListners;

import android.view.KeyEvent;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.structures.InputSpecialKeyEventCode;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.views.NumberKeypadView;

public class NumberPadKeyboardActionListener extends KeyboardActionListner {

    public NumberPadKeyboardActionListener(MainInputMethodService inputMethodService
            , NumberKeypadView view) {
        super(inputMethodService, view);
    }

    @Override
    protected boolean ExtendedOnKey(int primaryCode, int[] keyCodes) {

        switch (primaryCode) {
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToSymbolsKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_SYMBOLS_KEYBOARD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToSymbolsKeyboard);
                return true;
        }
        return false;
    }
}
