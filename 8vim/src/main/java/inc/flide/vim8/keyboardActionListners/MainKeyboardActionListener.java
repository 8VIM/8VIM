package inc.flide.vim8.keyboardActionListners;

import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.MovementSequenceType;

public class MainKeyboardActionListener {

    private final Handler longPressHandler = new Handler();
    private final MainInputMethodService mainInputMethodService;
    private final View mainKeyboardView;
    private final Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;
    private final List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private boolean isLongPressCallbackSet;
    private MovementSequenceType currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            List<FingerPosition> movementSequenceAgumented = new ArrayList<>(movementSequence);
            movementSequenceAgumented.add(FingerPosition.LONG_PRESS);
            processMovementSequence(movementSequenceAgumented);
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION);
        }
    };

    public MainKeyboardActionListener(MainInputMethodService inputMethodService,
                                      View view) {
        this.mainInputMethodService = inputMethodService;
        this.mainKeyboardView = view;

        keyboardActionMap = mainInputMethodService.buildKeyboardActionMap();

        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    public void movementStarted(FingerPosition fingerPosition) {
        currentFingerPosition = fingerPosition;
        movementSequence.clear();
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT;
        movementSequence.add(currentFingerPosition);
        initiateLongPressDetection();
    }

    public void movementContinues(FingerPosition fingerPosition) {
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = fingerPosition;

        boolean isFingerPositionChanged = (lastKnownFingerPosition != currentFingerPosition);

        if (isFingerPositionChanged) {
            interruptLongPress();
            movementSequence.add(currentFingerPosition);
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && keyboardActionMap.get(movementSequence) != null) {
                processMovementSequence(movementSequence);
                movementSequence.clear();
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT;
                movementSequence.add(currentFingerPosition);
            }
        } else if (!isLongPressCallbackSet) {
            initiateLongPressDetection();
        }
    }

    public void movementEnds() {
        interruptLongPress();
        currentFingerPosition = FingerPosition.NO_TOUCH;
        movementSequence.add(currentFingerPosition);
        processMovementSequence(movementSequence);
        movementSequence.clear();
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    }

    public void movementCanceled(){
        longPressHandler.removeCallbacks(longPressRunnable);
        isLongPressCallbackSet = false;
        movementSequence.clear();
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    }

    private void initiateLongPressDetection() {
        isLongPressCallbackSet = true;
        longPressHandler.postDelayed(longPressRunnable, Constants.DELAY_MILLIS_LONG_PRESS_INITIATION);
    }

    private void interruptLongPress() {
        longPressHandler.removeCallbacks(longPressRunnable);
        List<FingerPosition> movementSequenceAgumented = new ArrayList<>(movementSequence);
        movementSequenceAgumented.add(FingerPosition.LONG_PRESS_END);
        processMovementSequence(movementSequenceAgumented);
        isLongPressCallbackSet = false;
    }

    private void processMovementSequence(List<FingerPosition> movementSequence) {

        KeyboardAction keyboardAction = keyboardActionMap.get(movementSequence);

        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
            modifiedMovementSequence.add(0, FingerPosition.NO_TOUCH);
            keyboardAction = keyboardActionMap.get(modifiedMovementSequence);
        }

        boolean isMovementValid = true;
        if (keyboardAction == null) {
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()) {
            case INPUT_TEXT:
                mainInputMethodService.handleInputText(keyboardAction);
                processPredictiveTextCandidates();
                break;
            case INPUT_KEY:
                mainInputMethodService.handleInputKey(keyboardAction);
                break;
            case INPUT_SPECIAL:
                mainInputMethodService.handleSpecialInput(keyboardAction);
                break;
            default:
                isMovementValid = false;
        }
        if (isMovementValid) {
            mainKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void processPredictiveTextCandidates() {
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

    public void handleSpecialInput(KeyboardAction keyboardAction) {
        this.mainInputMethodService.handleSpecialInput(keyboardAction);
    }

    public boolean isShiftSet() {
        return mainInputMethodService.getShiftLockFlag() == KeyEvent.META_SHIFT_ON ;
    }

    public boolean isCapsLockSet() {
        return mainInputMethodService.getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }
}
