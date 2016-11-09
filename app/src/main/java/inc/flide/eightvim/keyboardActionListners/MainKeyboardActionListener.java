package inc.flide.eightvim.keyboardActionListners;

import android.view.HapticFeedbackConstants;

import java.util.List;
import java.util.Map;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.MainKeyboardView;
import inc.flide.logging.Logger;

public class MainKeyboardActionListener {


    private EightVimInputMethodService eightVimInputMethodService;
    private MainKeyboardView mainKeyboardView;

    Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

    public MainKeyboardActionListener(EightVimInputMethodService inputMethodService,
                                      MainKeyboardView view) {
        this.eightVimInputMethodService = inputMethodService;
        this.mainKeyboardView = view;
        keyboardActionMap = eightVimInputMethodService.buildKeyboardActionMap();
    }

    public void processMovementSequence(List<FingerPosition> movementSequence) {

        KeyboardAction keyboardAction = keyboardActionMap.get(movementSequence);

        boolean isMovementValid = true;
        if(keyboardAction == null){
            Logger.Verbose(this, "No Action Mapping has been defined for the given Sequence : " + movementSequence.toString());
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()){
            case INPUT_TEXT:
                eightVimInputMethodService.handleInputText(keyboardAction);
                break;
            case INPUT_KEY:
                eightVimInputMethodService.handleInputKey(keyboardAction);
                break;
            case INPUT_SPECIAL:
                eightVimInputMethodService.handleSpecialInput(keyboardAction);
                break;
            default:
                Logger.Warn(this, "Action Type Undefined : " + keyboardAction.getKeyboardActionType().toString());
                isMovementValid = false;
                break;
        }
        if(isMovementValid){
            mainKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}
