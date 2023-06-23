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
import inc.flide.vim8.structures.yaml.ExtraLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainKeypadActionListener extends KeypadActionListener {
    private static final int FULL_ROTATION_STEPS = 7;
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
    private final Set<List<FingerPosition>> extraLayerMovementSequences = new HashSet<>();
    private final Handler longPressHandler = new Handler();
    private final List<FingerPosition> movementSequence;
    private final Set<List<FingerPosition>> rotationMovementSequences = new HashSet<>();
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

        for (FingerPosition[] movementSequences : ROTATION_MOVEMENT_SEQUENCES) {
            rotationMovementSequences.add(Arrays.asList(movementSequences));
        }

        extraLayerMovementSequences.addAll(ExtraLayer.MOVEMENT_SEQUENCES.values());

        keyboardData = mainInputMethodService.buildKeyboardActionMap();
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    public static void rebuildKeyboardData(Resources resources, Context context) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resources, context);
    }

    public static void rebuildKeyboardData(Resources resources, Context context, Uri customLayoutUri) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resources, context,
                customLayoutUri);
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
    public int findLayer() {
        for (int i = ExtraLayer.values().length - 1; i >= 0; i--) {
            ExtraLayer extraLayer = ExtraLayer.values()[i];
            List<FingerPosition> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
            if (extraLayerMovementSequence == null) {
                return Constants.DEFAULT_LAYER;
            }

            if (movementSequence.size() < extraLayerMovementSequence.size()) {
                continue;
            }

            List<FingerPosition> startWith = movementSequence.subList(0, extraLayerMovementSequence.size());
            if (extraLayerMovementSequences.contains(startWith)) {
                return i + 2;
            }
        }
        return Constants.DEFAULT_LAYER;
    }

    private boolean isFullRotation() {
        int layer = findLayer();
        int size = FULL_ROTATION_STEPS;
        int start = 1;
        boolean layerCondition = movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;

        if (layer > Constants.DEFAULT_LAYER) {
            ExtraLayer extraLayer = ExtraLayer.values()[layer - 2];
            List<FingerPosition> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);

            if (extraLayerMovementSequence != null) {
                size += extraLayerMovementSequence.size();
                start += extraLayerMovementSequence.size();
                layerCondition = extraLayerMovementSequences.contains(
                        movementSequence.subList(0, extraLayerMovementSequence.size()));
            }
        }
        if (movementSequence.size() == size && layerCondition) {
            return rotationMovementSequences.contains(movementSequence.subList(start, size));
        }
        return false;
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
            if (isFullRotation()) {
                int start = 2;
                int size = FULL_ROTATION_STEPS - 1;
                int layer = findLayer();
                if (layer > Constants.DEFAULT_LAYER) {
                    ExtraLayer extraLayer = ExtraLayer.values()[layer - 2];
                    List<FingerPosition> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
                    if (extraLayerMovementSequence != null) {
                        start += extraLayerMovementSequence.size();
                        size += extraLayerMovementSequence.size();
                    }
                }
                movementSequence.subList(start, size).clear();
                mainInputMethodService.performShiftToggle();
            }

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
                    currentLetter = areCharactersCapitalized() ? action.getCapsLockText() : action.getText();
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
        List<FingerPosition> movementSequenceAugmented = new ArrayList<>(movementSequence);
        movementSequenceAugmented.add(FingerPosition.LONG_PRESS_END);
        processMovementSequence(movementSequenceAugmented);
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

        if (keyboardAction.getKeyboardActionType() == KeyboardActionType.INPUT_TEXT) {
            handleInputText(keyboardAction);
        } else {
            handleInputKey(keyboardAction);
        }
    }
}
