package inc.flide.vi8.keyboardActionListners;

import android.view.KeyEvent;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.structures.KeyboardActionType;
import inc.flide.vi8.views.SymbolKeyboardView;

public class SymbolKeyboardActionListener extends KeyboardActionListner {

    public SymbolKeyboardActionListener(MainInputMethodService inputMethodService
                                            , SymbolKeyboardView view){
        super(inputMethodService, view);
    }

    @Override
    protected boolean ExtendedOnkey(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToNumberPadKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_NUMBER_PAD.toString()
                        , null, 0, 0);
                mainInputMethodService.handleSpecialInput(switchToNumberPadKeyboardView);
                return true;
        }
        return false;
    }
}
