package inc.flide.vi8.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.vi8.MainInputMethodService;
import inc.flide.vi8.structures.InputSpecialKeyEventCode;
import inc.flide.vi8.keyboardHelpers.KeyboardAction;
import inc.flide.vi8.structures.KeyboardActionType;
import inc.flide.vi8.structures.SelectionKeyboardKeyCode;
import inc.flide.vi8.views.SelectionKeyboardView;

public class SelectionKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private MainInputMethodService mainInputMethodService;
    private SelectionKeyboardView selectionKeyboardView;

    private boolean isSelectionOn = true;

    public SelectionKeyboardActionListener(MainInputMethodService inputMethodService
                                            , SelectionKeyboardView view){
        this.mainInputMethodService = inputMethodService;
        this.selectionKeyboardView = view;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        SelectionKeyboardKeyCode selectionKeyboardKeyCode = SelectionKeyboardKeyCode.getAssociatedSelectionKeyCode(primaryCode);
        if (selectionKeyboardKeyCode == null) {
            return;
        }

        switch(selectionKeyboardKeyCode) {
            case CUT:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON);
                break;
            case COPY:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON);;
                break;
            case PASTE:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON);;
                break;
            case SELECT_ALL:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                KeyboardAction switchToMainKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0,0);
                mainInputMethodService.handleSpecialInput(switchToMainKeyboardView);
                break;
            case SWITCH_TO_EMOJI_KEYBOARD:
                KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_EMOJI_KEYBOARD.toString()
                        , null, 0,0);
                mainInputMethodService.handleSpecialInput(switchToEmojiKeyboard);
            case TOOGLE_SELECTION_MODE:
                isSelectionOn = !isSelectionOn;
                break;
            case DELETE_SELECTION:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_FORWARD_DEL,0);
                break;
            case MOVE_CURRENT_END_POINT_LEFT:
                moveSelection(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case MOVE_CURRENT_END_POINT_RIGHT:
                moveSelection(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case MOVE_CURRENT_END_POINT_DOWN:
                moveSelection(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case MOVE_CURRENT_END_POINT_UP:
                moveSelection(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case BACKSPACE:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DEL,0);
                break;
            case ENTER:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER,0);
                break;
        }

        selectionKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
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

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
