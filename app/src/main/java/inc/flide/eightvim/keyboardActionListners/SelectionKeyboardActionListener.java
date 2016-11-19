package inc.flide.eightvim.keyboardActionListners;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.structures.KeyboardActionType;
import inc.flide.eightvim.structures.SelectionKeyCode;
import inc.flide.eightvim.views.SelectionKeyboardView;

public class SelectionKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;
    private SelectionKeyboardView selectionKeyboardView;

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

        SelectionKeyCode selectionKeyCode = SelectionKeyCode.getAssociatedSelectionKeyCode(primaryCode);
        if (selectionKeyCode == null) {
            return;
        }

        switch(selectionKeyCode) {
            case CUT:
                eightVimInputMethodService.cut();
                break;
            case COPY:
                eightVimInputMethodService.copy();
                break;
            case PASTE:
                eightVimInputMethodService.paste();
                break;
            case SELECT_ALL:
                selectAll();
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);

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
        }

        selectionKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void selectAll() {
        eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
    }

    private void moveSelection(int dpad_keyCode) {
        eightVimInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        eightVimInputMethodService.sendDownAndUpKeyEvent(dpad_keyCode, 0);
        eightVimInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
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
