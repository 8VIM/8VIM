package inc.flide.vim8.keyboardactionlisteners;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
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
import java.util.stream.Collectors;

public class MainKeypadActionListener extends KeypadActionListener {
    private static final int FULL_ROTATION_STEPS = 7;
    private static final Set<List<Integer>> extraLayerMovementSequences = new HashSet<>(
            ExtraLayer.MOVEMENT_SEQUENCES.values());
    private static final Set<List<Integer>> ROTATION_MOVEMENT_SEQUENCES = Arrays
            .stream(new Integer[][] {
                    {1, 2, 3, 4, 5, 6},
                    {1, 6, 5, 4, 3, 2},
                    {2, 3, 4, 5, 6, 1},
                    {2, 1, 6, 5, 4, 3},
                    {3, 4, 5, 6, 1, 2},
                    {3, 2, 1, 6, 5, 4},
                    {4, 5, 6, 1, 2, 3},
                    {4, 3, 2, 1, 6, 5},
                    {5, 6, 1, 2, 3, 4},
                    {5, 4, 3, 2, 1, 6},
                    {6, 1, 2, 3, 4, 5},
                    {6, 5, 4, 3, 2, 1}})
            .map(Arrays::asList).collect(Collectors.toSet());
    private static KeyboardData keyboardData;
    private final List<Integer> movementSequence;
    private final Handler longPressHandler;
    private int currentFingerPosition;
    private String currentLetter;
    private boolean isLongPressCallbackSet;
    private MovementSequenceType currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            List<Integer> movementSequenceAugmented = new ArrayList<>(movementSequence);
            movementSequenceAugmented.add(FingerPosition.LONG_PRESS);
            processMovementSequence(movementSequenceAugmented);
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION);
        }
    };

    public MainKeypadActionListener(MainInputMethodService inputMethodService, View view) {
        super(inputMethodService, view);
        keyboardData = mainInputMethodService.buildKeyboardActionMap();
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
        HandlerThread longPressHandlerThread = new HandlerThread("LongPressHandlerThread");
        longPressHandlerThread.start();
        longPressHandler = new Handler(longPressHandlerThread.getLooper(), null);
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
            List<Integer> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
            if (extraLayerMovementSequence == null) {
                return Constants.DEFAULT_LAYER;
            }

            if (movementSequence.size() < extraLayerMovementSequence.size()) {
                continue;
            }

            List<Integer> startWith = movementSequence.subList(0, extraLayerMovementSequence.size());
            int layer = i + 2;
            if (extraLayerMovementSequences.contains(startWith) && layer <= keyboardData.getTotalLayers()) {
                return layer;
            }
        }
        List<Integer> tempMovementSequence = new ArrayList<>(movementSequence);
        tempMovementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return keyboardData.findLayer(tempMovementSequence);
    }

    private boolean isFullRotation() {
        int layer = findLayer();
        int size = FULL_ROTATION_STEPS;
        int start = 1;
        boolean layerCondition = movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;

        if (layer > Constants.DEFAULT_LAYER) {
            ExtraLayer extraLayer = ExtraLayer.values()[layer - 2];
            List<Integer> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);

            if (extraLayerMovementSequence != null) {
                size += extraLayerMovementSequence.size();
                start += extraLayerMovementSequence.size();
                layerCondition = extraLayerMovementSequences.contains(
                        movementSequence.subList(0, extraLayerMovementSequence.size()));
            }
        }
        if (movementSequence.size() == size && layerCondition) {
            return ROTATION_MOVEMENT_SEQUENCES.contains(movementSequence.subList(start, size));
        }
        return false;
    }

    public void movementStarted(int fingerPosition) {
        currentFingerPosition = fingerPosition;
        movementSequence.clear();
        currentLetter = null;
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT;
        movementSequence.add(currentFingerPosition);
        initiateLongPressDetection();
    }

    public void movementContinues(int fingerPosition) {
        int lastKnownFingerPosition = currentFingerPosition;
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
                    List<Integer> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
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
            } else if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE) {
                int layer = findLayer();
                List<Integer> extraLayerMovementSequences = new ArrayList<>();
                int layerSize = 0;
                if (layer > Constants.DEFAULT_LAYER) {
                    extraLayerMovementSequences = ExtraLayer.MOVEMENT_SEQUENCES.get(ExtraLayer.values()[layer - 2]);
                    layerSize = extraLayerMovementSequences.size() + 1;
                }
                boolean defaultLayerCondition = layer == Constants.DEFAULT_LAYER
                        && movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;
                boolean extraLayerCondition = layer > Constants.DEFAULT_LAYER && movementSequence.size() > layerSize;
                if (defaultLayerCondition || extraLayerCondition) {
                    movementSequence.clear();
                    currentLetter = null;
                    currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT;
                    movementSequence.addAll(extraLayerMovementSequences);
                    movementSequence.add(currentFingerPosition);
                }
            } else {
                List<Integer> modifiedMovementSequence = new ArrayList<>(movementSequence);
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
        List<Integer> movementSequenceAugmented = new ArrayList<>(movementSequence);
        movementSequenceAugmented.add(FingerPosition.LONG_PRESS_END);
        processMovementSequence(movementSequenceAugmented);
        isLongPressCallbackSet = false;
    }

    private void processMovementSequence(List<Integer> movementSequence) {

        KeyboardAction keyboardAction = keyboardData.getActionMap().get(movementSequence);
        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            List<Integer> modifiedMovementSequence = new ArrayList<>(movementSequence);
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
            handleInputKey(keyboardAction.getKeyEventCode(), keyboardAction.getKeyFlags());
        }
    }

    public static int getLayoutPositions() {
        return keyboardData.layoutPositions;
    }
}
