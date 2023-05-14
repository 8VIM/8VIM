package inc.flide.vim8.keyboardactionlisteners;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardhelpers.InputMethodServiceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.MovementSequenceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainKeypadActionListener extends KeypadActionListener {
    private static final int FULL_ROTATION_STEPS = 6;
    private static final FingerPosition[][] ROTATION_MOVEMENT_SEQUENCES = {
            {FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT,
                    FingerPosition.BOTTOM, FingerPosition.LEFT},
            {FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT,
                    FingerPosition.BOTTOM, FingerPosition.RIGHT},
            {FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT,
                    FingerPosition.TOP},
            {FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT,
                    FingerPosition.BOTTOM},
            {FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP,
                    FingerPosition.LEFT},
            {FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP,
                    FingerPosition.RIGHT},
            {FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT,
                    FingerPosition.TOP},
            {FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT,
                    FingerPosition.BOTTOM},
    };
    private static KeyboardData keyboardData;
    private final Handler longPressHandler = new Handler();
    private final List<FingerPosition> movementSequence;
    private final HashSet<List<FingerPosition>> rotationMovementSequences;
    private FingerPosition currentFingerPosition;
    private String currentLetter;
    private boolean isLongPressCallbackSet;
    private MovementSequenceType currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            List<FingerPosition> movementSequenceAugmented = new ArrayList<>(movementSequence);
            movementSequenceAugmented.add(FingerPosition.LONG_PRESS);
            processMovementSequence(movementSequenceAugmented);
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION);
        }
    };

    public MainKeypadActionListener(MainInputMethodService inputMethodService, View view) {
        super(inputMethodService, view);
        rotationMovementSequences = new HashSet<>();

        for (FingerPosition[] movementSequences : ROTATION_MOVEMENT_SEQUENCES) {
            rotationMovementSequences.add(Arrays.asList(movementSequences));
        }

        keyboardData = mainInputMethodService.buildKeyboardActionMap();
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    public static void rebuildKeyboardData(Resources resource, Context context) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resource, context);
    }

    public static void rebuildKeyboardData(Resources resource, Context context, Uri customLayoutUri) {
        keyboardData =
                InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resource, context, customLayoutUri);
    }

    public String getLowerCaseCharacters(int layer) {
        return keyboardData.getLowerCaseCharacters(layer);
    }

    public String getUpperCaseCharacters(int layer) {
        return keyboardData.getUpperCaseCharacters(layer);
    }

    public String getCurrentLetter() {
        return currentLetter;
    }

    @Override
    public boolean isCircleCapitalization() {
        return isFullRotation();
    }

    @Override
    public int findLayer() {
        if (movementSequence.isEmpty() || movementSequence.get(0) != FingerPosition.INSIDE_CIRCLE) {
            return Constants.DEFAULT_LAYER;
        }
        List<FingerPosition> tempMovementSequence = new ArrayList<>(movementSequence);
        if (isFullRotation()) {
            tempMovementSequence = tempMovementSequence.subList(FULL_ROTATION_STEPS - 1, movementSequence.size());
            tempMovementSequence.add(0, FingerPosition.INSIDE_CIRCLE);
        }
        tempMovementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return keyboardData.findLayer(tempMovementSequence);
    }

    private boolean isFullRotation() {
        if (movementSequence.size() < 7 || movementSequence.get(0) != FingerPosition.INSIDE_CIRCLE) {
            return false;
        }
        return rotationMovementSequences.contains(movementSequence.subList(1, FULL_ROTATION_STEPS + 1));

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
            List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
            if (isCircleCapitalization()) {
                modifiedMovementSequence.subList(1, FULL_ROTATION_STEPS - 1).clear();

            }
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && keyboardData.getActionMap().get(modifiedMovementSequence) != null) {
                processMovementSequence(modifiedMovementSequence);
                movementSequence.clear();
                currentLetter = null;
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT;
                movementSequence.add(currentFingerPosition);
            } else if (currentFingerPosition != FingerPosition.INSIDE_CIRCLE) {
                modifiedMovementSequence.add(FingerPosition.INSIDE_CIRCLE);
                KeyboardAction action = keyboardData.getActionMap().get(modifiedMovementSequence);

                if (action != null) {
                    currentLetter = isCircleCapitalization() ? action.getCapsLockText() : action.getText();
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
        if (keyboardAction == null && isCircleCapitalization()) {
            List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
            modifiedMovementSequence.subList(1, FULL_ROTATION_STEPS - 1).clear();
            keyboardAction = keyboardData.getActionMap().get(modifiedMovementSequence);

        }
        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            List<FingerPosition> modifiedMovementSequence = new ArrayList<>(movementSequence);
            modifiedMovementSequence.add(0, FingerPosition.NO_TOUCH);
            keyboardAction = keyboardData.getActionMap().get(modifiedMovementSequence);
        }

        if (keyboardAction == null) {
            movementSequence.clear();
            return;
        }

        if (keyboardAction.getKeyboardActionType() == KeyboardActionType.INPUT_KEY) {
            handleInputText(keyboardAction);
        } else {
            handleInputKey(keyboardAction);
        }
    }
}
