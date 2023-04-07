package inc.flide.vim8.keyboardActionListners;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.MovementSequenceType;
import inc.flide.vim8.structures.TrieNode;

public class MainKeypadActionListener extends KeypadActionListener {
    private static final int FULL_ROTATION_STEPS = 6;
    private final Handler longPressHandler = new Handler();
    private static final FingerPosition[][] ROTATION_MOVEMENT_SEQUENCES = new FingerPosition[][]{
            new FingerPosition[]{FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT},
            new FingerPosition[]{FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT},
            new FingerPosition[]{FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP},
            new FingerPosition[]{FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM},
            new FingerPosition[]{FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT},
            new FingerPosition[]{FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT},
            new FingerPosition[]{FingerPosition.RIGHT, FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT, FingerPosition.TOP},
            new FingerPosition[]{FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT, FingerPosition.BOTTOM},
    };
    private static KeyboardData keyboardData;
    private final List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private String currentLetter;
    private boolean isLongPressCallbackSet;
    private MovementSequenceType currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    private final TrieNode rotationMovementSequences;

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
        rotationMovementSequences = new TrieNode();

        for (FingerPosition[] movementSequences : ROTATION_MOVEMENT_SEQUENCES) {
           rotationMovementSequences.addMovementSequence(Arrays.asList(movementSequences), 1);
        }

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
        return isFullRotation() && movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;
    }

    @Override
    public int findLayer() {
        List<FingerPosition> tempMovementSequence = movementSequence;
        if (isFullRotation()) {
            tempMovementSequence = movementSequence.subList(FULL_ROTATION_STEPS - 1,movementSequence.size());
        }
        return keyboardData.findLayer(tempMovementSequence);
    }

    private boolean isFullRotation() {
        int size = movementSequence.size();
        if (size < 7) return false;

        return rotationMovementSequences.findLayer(movementSequence.subList(0, FULL_ROTATION_STEPS + 1)) == 1;

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
