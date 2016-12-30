package inc.flide.eightvim.keyboardActionListners;

import android.os.Handler;
import android.view.HapticFeedbackConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.structures.Constants;
import inc.flide.eightvim.structures.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.views.MainKeyboardView;
import inc.flide.logging.Logger;

public class MainKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;
    private MainKeyboardView mainKeyboardView;

    private Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

    private List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private boolean isLongPressCallbackSet;

    public MainKeyboardActionListener(EightVimInputMethodService inputMethodService,
                                      MainKeyboardView view) {
        this.eightVimInputMethodService = inputMethodService;
        this.mainKeyboardView = view;

        keyboardActionMap = eightVimInputMethodService.buildKeyboardActionMap();

        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    public void movementStarted(FingerPosition fingerPosition) {
        currentFingerPosition = fingerPosition;
        movementSequence.clear();
        movementSequence.add(currentFingerPosition);
        initiateLongPressDetection();
    }

    public void movementContinues(FingerPosition fingerPosition) {
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = fingerPosition;

        boolean isFingerPositionChanged = (lastKnownFingerPosition != currentFingerPosition);

        if(isFingerPositionChanged){
            interruptLongPress();
            movementSequence.add(currentFingerPosition);
            if(currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && keyboardActionMap.get(movementSequence)!=null){
                processMovementSequence(movementSequence);
                movementSequence.clear();
                movementSequence.add(currentFingerPosition);
            }
        }else if(!isLongPressCallbackSet){
            initiateLongPressDetection();
        }
    }

    public void movementEnds() {
        interruptLongPress();
        currentFingerPosition = FingerPosition.NO_TOUCH;
        movementSequence.add(currentFingerPosition);
        processMovementSequence(movementSequence);
        movementSequence.clear();
    }

    private final Handler longPressHandler = new Handler();
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            List<FingerPosition> movementSequenceAgumented = new ArrayList<>(movementSequence);
            movementSequenceAgumented.add(FingerPosition.LONG_PRESS);
            processMovementSequence(movementSequenceAgumented);
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION);
        }
    };

    private void initiateLongPressDetection(){
        isLongPressCallbackSet = true;
        longPressHandler.postDelayed(longPressRunnable, Constants.DELAY_MILLIS_LONG_PRESS_INITIATION);
    }

    private void interruptLongPress(){
        longPressHandler.removeCallbacks(longPressRunnable);
        List<FingerPosition> movementSequenceAgumented = new ArrayList<>(movementSequence);
        movementSequenceAgumented.add(FingerPosition.LONG_PRESS_END);
        processMovementSequence(movementSequenceAgumented);
        isLongPressCallbackSet = false;
    }

    private void processMovementSequence(List<FingerPosition> movementSequence) {

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
        }
        if(isMovementValid){
            mainKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}
