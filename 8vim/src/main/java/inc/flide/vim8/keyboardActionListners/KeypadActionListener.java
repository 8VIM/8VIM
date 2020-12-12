package inc.flide.vim8.keyboardActionListners;

import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.structures.InputSpecialKeyEventCode;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.SelectionKeyboardKeyCode;

public class KeypadActionListener {

    protected MainInputMethodService mainInputMethodService;
    protected View view;
    private boolean isSelectionOn = true;

    public KeypadActionListener(MainInputMethodService mainInputMethodService, View view) {
        this.mainInputMethodService = mainInputMethodService;
        this.view = view;
    }

    public void onKey(int primaryCode, int[] keyCodes) {

        switch (primaryCode) {
            case KeyEvent.KEYCODE_EISU:
                KeyboardAction switchToMainKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0, 0);
                handleSpecialInput(switchToMainKeyboardView);
                break;
            case KeyEvent.KEYCODE_NUM_LOCK:
                KeyboardAction switchToNumberPadKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_NUMBER_PAD.toString()
                        , null, 0, 0);
                handleSpecialInput(switchToNumberPadKeyboardView);
                break;
            case KeyEvent.KEYCODE_CUT:
                mainInputMethodService.cut();
                break;
            case KeyEvent.KEYCODE_COPY:
                mainInputMethodService.copy();
                break;
            case KeyEvent.KEYCODE_PASTE:
                mainInputMethodService.paste();
                break;
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
            case KeyEvent.KEYCODE_TAB:
                mainInputMethodService.sendKey(primaryCode, 0);
                break;
            case KeyEvent.KEYCODE_ENTER:
                mainInputMethodService.commitImeOptionsBasedEnter();
                break;
            default:
                if (!ExtendedOnKey(primaryCode, keyCodes)) {
                    mainInputMethodService.sendText(String.valueOf((char) primaryCode));
                }
        }

        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    private boolean ExtendedOnKey(int primaryCode, int[] keyCodes) {

        SelectionKeyboardKeyCode selectionKeyboardKeyCode = SelectionKeyboardKeyCode.getAssociatedSelectionKeyCode(primaryCode);
        if (selectionKeyboardKeyCode == null) {
            return false;
        }

        switch (selectionKeyboardKeyCode) {
            case SELECT_ALL:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                return true;
            case TOGGLE_SELECTION_MODE:
                isSelectionOn = !isSelectionOn;
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
            case SWITCH_TO_SYMBOLS_KEYPAD:
                KeyboardAction switchToSymbolsKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_SYMBOLS_KEYBOARD.toString()
                        , null, 0, 0);
                handleSpecialInput(switchToSymbolsKeyboard);
                return true;
        }

        return false;
    }

    private void moveSelection(int dpad_keyCode) {
        if (isSelectionOn) {
            mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        }
        mainInputMethodService.sendDownAndUpKeyEvent(dpad_keyCode, 0);
        if (isSelectionOn) {
            mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
        }
    }

    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if (keyboardAction.getText().length() == 1
                && (isShiftSet() || isCapsLockSet())) {
            mainInputMethodService.sendText(keyboardAction.getCapsLockText());
            mainInputMethodService.setShiftLockFlag(0);
        } else {
            mainInputMethodService.sendText(keyboardAction.getText());
        }
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
        mainInputMethodService.setShiftLockFlag(0);
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {

        InputSpecialKeyEventCode keyeventCode = InputSpecialKeyEventCode.valueOf(keyboardAction.getText());

        switch (keyeventCode) {
            case SHIFT_TOOGLE:
                mainInputMethodService.performShiftToggle();
                break;
            case SWITCH_TO_EMOJI_KEYBOARD:
                mainInputMethodService.switchToExternalEmoticonKeyboard();
                break;
            case SWITCH_TO_NUMBER_PAD:
                mainInputMethodService.switchToNumberPad();
                break;
            case SWITCH_TO_MAIN_KEYBOARD:
                mainInputMethodService.switchToMainKeypad();
                break;
            case SWITCH_TO_SYMBOLS_KEYBOARD:
                mainInputMethodService.switchToSymbolsKeypad();
                break;
            case PASTE:
                mainInputMethodService.paste();
                break;
            case SELECTION_START:
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0);
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                break;
            case SWITCH_TO_SELECTION_KEYBOARD:
                mainInputMethodService.switchToSelectionKeypad();
                break;
            case HIDE_KEYBOARD:
                mainInputMethodService.hideKeyboard();
                break;
            default:
                break;
        }
    }
    public boolean areCharactersCapitalized() {
        return mainInputMethodService.areCharactersCapitalized();
    }

    public void setModifierFlags(int modifierFlags) {
        this.mainInputMethodService.setModifierFlags(modifierFlags);
    }

    public void sendKey(int keycode, int flags) {
        this.mainInputMethodService.sendKey(keycode, flags);
    }

    public boolean isShiftSet() {
        return mainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON ;
    }

    public boolean isCapsLockSet() {
        return mainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }
}
