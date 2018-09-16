package inc.flide.vi8.keyboardActionListners;

import android.view.KeyEvent;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.KeyboardActionType;
import inc.flide.vi8.structures.SelectionKeyboardKeyCode;
import inc.flide.vi8.views.SelectionKeyboardView;

public class SelectionKeyboardActionListener extends KeyboardActionListner {

    private boolean isSelectionOn = true;

    public SelectionKeyboardActionListener(MainInputMethodService inputMethodService
                                            , SelectionKeyboardView view){
        super(inputMethodService, view);
    }

    protected boolean ExtendedOnkey(int primaryCode, int[] keyCodes) {

        SelectionKeyboardKeyCode selectionKeyboardKeyCode = SelectionKeyboardKeyCode.getAssociatedSelectionKeyCode(primaryCode);
        if (selectionKeyboardKeyCode == null) {
            return false;
        }

        switch(selectionKeyboardKeyCode) {
            case CUT:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON);
                return true;
            case COPY:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON);;
                return true;
            case PASTE:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON);;
                return true;
            case SELECT_ALL:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                return true;
            case SWITCH_TO_MAIN_KEYBOARD:
                KeyboardAction switchToMainKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0,0);
                mainInputMethodService.handleSpecialInput(switchToMainKeyboardView);
                return true;
            case SWITCH_TO_EMOJI_KEYBOARD:
                KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_EMOJI_KEYBOARD.toString()
                        , null, 0,0);
                mainInputMethodService.handleSpecialInput(switchToEmojiKeyboard);
            case TOOGLE_SELECTION_MODE:
                isSelectionOn = !isSelectionOn;
                return true;
            case DELETE_SELECTION:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_FORWARD_DEL,0);
                return true;
            case MOVE_CURRENT_END_POINT_LEFT:
                moveSelection(KeyEvent.KEYCODE_DPAD_LEFT);
                return true;
            case MOVE_CURRENT_END_POINT_RIGHT:
                moveSelection(KeyEvent.KEYCODE_DPAD_RIGHT);
                return true;
            case MOVE_CURRENT_END_POINT_DOWN:
                moveSelection(KeyEvent.KEYCODE_DPAD_DOWN);
                return true;
            case MOVE_CURRENT_END_POINT_UP:
                moveSelection(KeyEvent.KEYCODE_DPAD_UP);
                return true;
            case BACKSPACE:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DEL,0);
                return true;
            case ENTER:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER,0);
                return true;
        }

        return false;
    }

    private void moveSelection(int dpad_keyCode) {
        if(isSelectionOn){
            mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        }
        mainInputMethodService.sendDownAndUpKeyEvent(dpad_keyCode, 0);
        if(isSelectionOn){
            mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        }
    }

}
