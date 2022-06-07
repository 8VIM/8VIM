package inc.flide.vim8.keyboardActionListners;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.MovementSequenceType;

public class MainKeypadActionListener extends KeypadActionListener {

    private final Handler longPressHandler = new Handler();
    private static KeyboardData keyboardData;
    private final List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private String currentLetter;
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

    public MainKeypadActionListener(MainInputMethodService inputMethodService, View view) {
        super(inputMethodService, view);

        keyboardData = mainInputMethodService.buildKeyboardActionMap();
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    public static void rebuildKeyboardData(Resources resource, Context context) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resource, context);
    }

    public static void rebuildKeyboardData(Resources resource, Context context, Uri customLayoutUri) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resource, context, customLayoutUri);
    }

    public String getLowerCaseCharacters() {
        return keyboardData.getLowerCaseCharacters();
    }

    public String getUpperCaseCharacters() {
        return keyboardData.getUpperCaseCharacters();
    }

    public String getCurrentLetter() {
        return currentLetter;
    }

    @Override
    public boolean areCharactersCapitalized() {
        if (movementSequence.size() < 7) return super.areCharactersCapitalized();
        return true;
    }

    @Override
    public boolean isShiftSet() {
        if (movementSequence.size() < 7) return super.isShiftSet();
        return true;
    }

    public void movementStarted(FingerPosition fingerPosition) {
        currentFingerPosition = fingerPosition;
        movementSequence.clear();
        currentLetter = null;
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT;
        movementSequence.add(currentFingerPosition);
        initiateLongPressDetection();
    }

    public void movementContinues(FingerPosition fingerPosition) {
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = fingerPosition;

        boolean isFingerPositionChanged = lastKnownFingerPosition != currentFingerPosition;

        if (isFingerPositionChanged) {
            interruptLongPress();
            movementSequence.add(currentFingerPosition);
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && keyboardData.getActionMap().get(movementSequence) != null) {
                processMovementSequence(movementSequence);
                movementSequence.clear();
                currentLetter = null;
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT;
                movementSequence.add(currentFingerPosition);
            } else if (currentFingerPosition != FingerPosition.INSIDE_CIRCLE) {
                List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
                modifiedMovementSequence.add(FingerPosition.INSIDE_CIRCLE);
                KeyboardAction action = keyboardData.getActionMap().get(modifiedMovementSequence);

                if (action != null) {
                    currentLetter = action.getText();
                }
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
        currentLetter = null;
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    }

    public void movementCanceled() {
        longPressHandler.removeCallbacks(longPressRunnable);
        isLongPressCallbackSet = false;
        movementSequence.clear();
        currentLetter = null;
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

        KeyboardAction keyboardAction = keyboardData.getActionMap().get(movementSequence);

        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
            modifiedMovementSequence.add(0, FingerPosition.NO_TOUCH);
            keyboardAction = keyboardData.getActionMap().get(modifiedMovementSequence);
        }

        if (keyboardAction == null) {
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()) {
            case INPUT_TEXT:
                handleInputText(keyboardAction);
                break;
            case INPUT_KEY:
                handleInputKey(keyboardAction);
                break;
        }
    }
}
