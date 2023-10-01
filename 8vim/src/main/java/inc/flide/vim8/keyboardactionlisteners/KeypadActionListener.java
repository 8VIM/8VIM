package inc.flide.vim8.keyboardactionlisteners;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.CustomKeycode;
import inc.flide.vim8.models.KeyboardAction;
import inc.flide.vim8.models.LayerLevel;

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

    public float getCtrlAlpha() {
        return mainInputMethodService.getCtrlAlpha();
    }

    public void performCtrlToggle() {
        mainInputMethodService.performCtrlToggle();
    }

    public MainInputMethodService.State getCtrlState() {
        return mainInputMethodService.getCtrlState();
    }

    private int keySound(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN;
            case KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> AudioManager.FX_KEYPRESS_DELETE;
            case KeyEvent.KEYCODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR;
            default -> AudioManager.FX_KEYPRESS_STANDARD;
        };
    }

    private void performInputAcceptedFeedback(int keySound) {
        AppPrefs.InputFeedback prefs = appPreferenceModel().java().getInputFeedback();
        if (prefs.getHapticEnabled().get()) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        if (prefs.getSoundEnabled().get()) {
            audioManager.playSoundEffect(keySound);
        }
    }

    private boolean handleKeyEventKeyCodes(int primaryCode, int keyFlags) {
        if (keyCodeIsValid(primaryCode)) {
            switch (primaryCode) {
                case KeyEvent.KEYCODE_CUT -> mainInputMethodService.cut();
                case KeyEvent.KEYCODE_COPY -> mainInputMethodService.copy();
                case KeyEvent.KEYCODE_PASTE -> mainInputMethodService.paste();
                case KeyEvent.KEYCODE_ENTER -> mainInputMethodService.commitImeOptionsBasedEnter();
                case KeyEvent.KEYCODE_DEL -> mainInputMethodService.delete();
                default -> {
                    mainInputMethodService.sendKey(primaryCode, keyFlags);
                    mainInputMethodService.resetShiftState();
                }
            }
            return true;
        }

        return false;
    }

    public void onText(CharSequence text) {
        mainInputMethodService.sendText(text.toString());
        mainInputMethodService.resetShiftState();
        performInputAcceptedFeedback(AudioManager.FX_KEYPRESS_STANDARD);
    }

    public void handleInputText(KeyboardAction keyboardAction) {
        boolean isUpperCase = mainInputMethodService.getShiftstate() != MainInputMethodService.State.OFF;
        String text = (isUpperCase && !keyboardAction.getCapsLockText().isEmpty()) ? keyboardAction.getCapsLockText() :
                keyboardAction.getText();
        onText(text);
    }

    public boolean areCharactersCapitalized() {
        return mainInputMethodService.areCharactersCapitalized();
    }

    public boolean isShiftSet() {
        return mainInputMethodService.getShiftstate() == MainInputMethodService.State.ON;
    }

    public boolean isCapsLockSet() {
        return mainInputMethodService.getShiftstate() == MainInputMethodService.State.ENGAGED;
    }

    public LayerLevel findLayer() {
        return LayerLevel.FIRST;
    }
}
