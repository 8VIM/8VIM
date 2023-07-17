package inc.flide.vim8.keyboardactionlisteners;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.KeyboardAction;

public abstract class KeypadActionListener {
    public static final int KEYCODE_PROFILE_SWITCH = 288;
    private final AudioManager audioManager;
    protected MainInputMethodService mainInputMethodService;
    protected View view;

    public KeypadActionListener(MainInputMethodService mainInputMethodService, View view) {
        this.mainInputMethodService = mainInputMethodService;
        this.view = view;
        this.audioManager = (AudioManager) view.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    private boolean keyCodeIsValid(int keyCode) {
        int keycodeProfileSwitch = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? KeyEvent.KEYCODE_PROFILE_SWITCH :
                KEYCODE_PROFILE_SWITCH;
        return keyCode >= KeyEvent.KEYCODE_UNKNOWN && keyCode <= keycodeProfileSwitch;
    }

    public void handleInputKey(int keyCode, int keyFlags) {
        boolean actionHandled = handleKeyEventKeyCodes(keyCode, keyFlags);
        if (!actionHandled) {
            CustomKeycode customKeycode = CustomKeycode.fromIntValue(keyCode);
            if (customKeycode != null) {
                actionHandled = customKeycode.handleKeyCode(mainInputMethodService);
            }
        }
        if (!actionHandled) {
            onText(String.valueOf((char) keyCode));
        } else {
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
            default:
                return AudioManager.FX_KEYPRESS_STANDARD;
        }
    }

    @SuppressWarnings("deprecation")
    private void performInputAcceptedFeedback(int keySound) {
        SharedPreferenceHelper pref = SharedPreferenceHelper.getInstance(mainInputMethodService);
        boolean userEnabledHapticFeedback =
                pref.getBoolean(mainInputMethodService.getString(R.string.pref_haptic_feedback_key), true);
        if (userEnabledHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        boolean userEnabledSoundFeedback =
                pref.getBoolean(mainInputMethodService.getString(R.string.pref_sound_feedback_key), true);
        if (userEnabledSoundFeedback) {
            audioManager.playSoundEffect(keySound);
        }
    }

    private boolean handleKeyEventKeyCodes(int primaryCode, int keyFlags) {
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
        boolean isUpperCase = isShiftSet() || isCapsLockSet();
        String text = (isUpperCase && !keyboardAction.getCapsLockText().isEmpty()) ? keyboardAction.getCapsLockText() :
                keyboardAction.getText();
        onText(text);
    }

    public boolean areCharactersCapitalized() {
        return mainInputMethodService.areCharactersCapitalized();
    }

    public void setModifierFlags(int modifierFlags) {
        this.mainInputMethodService.setModifierFlags(modifierFlags);
    }

    public boolean isShiftSet() {
        return mainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON;
    }

    public boolean isCapsLockSet() {
        return mainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }

    public int findLayer() {
        return Constants.DEFAULT_LAYER;
    }
}
