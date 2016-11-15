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
            case SWITCH_TO_MAIN_KEYBOARD: {
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0);
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
            }
            break;
            case MOVE_CURRENT_END_POINT_LEFT: {

                InputConnection inputConnection = eightVimInputMethodService.getCurrentInputConnection();
                inputConnection.performContextMenuAction(android.R.id.startSelectingText);
                ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
                inputConnection.setSelection(extractedText.selectionStart - 1, extractedText.selectionEnd);

                eightVimInputMethodService.updateSelection(extractedText.selectionStart - 1, extractedText.selectionEnd);
                }
                break;
            case MOVE_CURRENT_END_POINT_RIGHT:{
                InputConnection inputConnection = eightVimInputMethodService.getCurrentInputConnection();
                ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
                inputConnection.setSelection(extractedText.selectionStart + 1, extractedText.selectionEnd);

                eightVimInputMethodService.updateSelection(extractedText.selectionStart - 1, extractedText.selectionEnd);
                }
                break;
        }

        selectionKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private void selectAll() {
        eightVimInputMethodService.getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_A,
                        0,
                        KeyEvent.META_CTRL_ON
                )
        );
        eightVimInputMethodService.getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_A,
                        0,
                        KeyEvent.META_CTRL_ON
                )
        );
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
