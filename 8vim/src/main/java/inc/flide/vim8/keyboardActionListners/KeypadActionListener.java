package inc.flide.vim8.keyboardActionListners;

import android.content.Context;
import android.media.AudioManager;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.CustomKeycode;

public class KeypadActionListener {

    protected MainInputMethodService mainInputMethodService;
    protected View view;
    private boolean isSelectionOn = true;
    private AudioManager audioManager;

    public KeypadActionListener(MainInputMethodService mainInputMethodService, View view) {
        this.mainInputMethodService = mainInputMethodService;
        this.view = view;
        this.audioManager = (AudioManager) view.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    private boolean keyCodeIsValid(int keyCode) {
        //return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= KeyEvent.KEYCODE_PROFILE_SWITCH;
        return keyCode >= 0 && keyCode <= 288;
    }

    private boolean customKeyCodeIsValid(int keyCode) {
        return keyCode <= KeyEvent.KEYCODE_UNKNOWN;
    }

    public void handleInputKey(KeyboardAction keyboardAction) {
        handleInputKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
    }

    public void handleInputKey(int keyCode, int keyFlags) {

        boolean actionHandled = handleKeyEventKeyCodes(keyCode, keyFlags);
        if (!actionHandled) {
            actionHandled = handleCustomKeyCodes(keyCode, keyFlags);
        }
        if (!actionHandled) {
            onText(String.valueOf((char) keyCode));
        }
        if (actionHandled) {
            performInputAcceptedFeedback(keySound(keyCode));
        }
    }

    private int keySound(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                return AudioManager.FX_KEYPRESS_RETURN;
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return AudioManager.FX_KEYPRESS_DELETE;
            case KeyEvent.KEYCODE_SPACE:
                return AudioManager.FX_KEYPRESS_SPACEBAR;
        }
        return AudioManager.FX_KEYPRESS_STANDARD;
    }
    private void performInputAcceptedFeedback(int keySound) {
        SharedPreferenceHelper pref = SharedPreferenceHelper.getInstance(mainInputMethodService);
        boolean user_enabled_haptic_feedback =
                pref.getBoolean(
                        mainInputMethodService.getString(R.string.pref_haptic_feedback_key),
                        true);
        if (user_enabled_haptic_feedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        boolean user_enabled_sound_feedback = pref
                .getBoolean(
                        mainInputMethodService.getString(R.string.pref_sound_feedback_key),
                        true);
        if (user_enabled_sound_feedback) {
            audioManager.playSoundEffect(keySound);
        }
    }

    private boolean handleCustomKeyCodes(int customKeyEventCode, int keyFlags) {
        switch (CustomKeycode.fromIntValue(customKeyEventCode)) {
            case MOVE_CURRENT_END_POINT_LEFT:
                moveSelection(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case MOVE_CURRENT_END_POINT_RIGHT:
                moveSelection(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case MOVE_CURRENT_END_POINT_UP:
                moveSelection(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case MOVE_CURRENT_END_POINT_DOWN:
                moveSelection(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case SELECTION_START:
                mainInputMethodService.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, 0);
                mainInputMethodService.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0);
                break;
            case SELECT_ALL:
                mainInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON);
                break;
            case TOGGLE_SELECTION_ANCHOR:
                mainInputMethodService.switchAnchor();
                break;
            case SHIFT_TOGGLE:
                mainInputMethodService.performShiftToggle();
                break;
            case SWITCH_TO_MAIN_KEYPAD:
                mainInputMethodService.switchToMainKeypad();
                break;
            case SWITCH_TO_NUMBER_KEYPAD:
                mainInputMethodService.switchToNumberPad();
                break;
            case SWITCH_TO_SYMBOLS_KEYPAD:
                mainInputMethodService.switchToSymbolsKeypad();
                break;
            case SWITCH_TO_SELECTION_KEYPAD:
                mainInputMethodService.switchToSelectionKeypad();
                break;
            case SWITCH_TO_EMOTICON_KEYBOARD:
                mainInputMethodService.switchToExternalEmoticonKeyboard();
                break;
            case HIDE_KEYBOARD:
                mainInputMethodService.hideKeyboard();
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean handleKeyEventKeyCodes(int primaryCode, int keyFlags) {
        if (keyCodeIsValid(primaryCode)) {
            switch (primaryCode) {
                case KeyEvent.KEYCODE_CUT:
                    mainInputMethodService.cut();
                    break;
                case KeyEvent.KEYCODE_COPY:
                    mainInputMethodService.copy();
                    break;
                case KeyEvent.KEYCODE_PASTE:
                    mainInputMethodService.paste();
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    mainInputMethodService.commitImeOptionsBasedEnter();
                    break;
                case KeyEvent.KEYCODE_DEL:
                    mainInputMethodService.delete();
                    break;
                default:
                    mainInputMethodService.sendKey(primaryCode, keyFlags);
                    mainInputMethodService.setShiftLockFlag(0);
            }
            return true;
        }

        return false;
    }

    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        mainInputMethodService.setShiftLockFlag(0);
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD);
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        if (keyboardAction.getText().length() == 1
                && (isShiftSet() || isCapsLockSet())) {
            onText(keyboardAction.getCapsLockText());
        } else {
            onText(keyboardAction.getText());
        }
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

    public boolean areCharactersCapitalized() {
        return mainInputMethodService.areCharactersCapitalized();
    }

    public void setModifierFlags(int modifierFlags) {
        this.mainInputMethodService.setModifierFlags(modifierFlags);
    }

    public boolean isShiftSet() {
        return mainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON ;
    }

    public boolean isCapsLockSet() {
        return mainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }
}
