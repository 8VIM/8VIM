package inc.flide.eightvim.keyboardActionListners;

import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.structures.KeyboardActionType;
import inc.flide.eightvim.structures.SelectionKeyboardKeyCode;
import inc.flide.eightvim.views.SelectionKeyboardView;

public class SelectionKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;
    private SelectionKeyboardView selectionKeyboardView;

    private boolean isSelectionOn = true;

    public SelectionKeyboardActionListener(EightVimInputMethodService inputMethodService
                                            , SelectionKeyboardView view){
        this.eightVimInputMethodService = inputMethodService;
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
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON);
                break;
            case COPY:
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON);;
                break;
            case PASTE:
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON);;
                break;
            case SELECT_ALL:
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case SWITCH_TO_EMOJI_KEYBOARD:
                KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_EMOJI_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToEmojiKeyboard);
            case TOOGLE_SELECTION_MODE:
                isSelectionOn = !isSelectionOn;
                break;
            case DELETE_SELECTION:
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_FORWARD_DEL,0);
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
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DEL,0);
                break;
            case ENTER:
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER,0);
                break;
        }

        selectionKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void moveSelection(int dpad_keyCode) {
        if(isSelectionOn){
            eightVimInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        }
        eightVimInputMethodService.sendDownAndUpKeyEvent(dpad_keyCode, 0);
        if(isSelectionOn){
            eightVimInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
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
