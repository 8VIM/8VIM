package inc.flide.vim8.keyboardactionlisteners;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.models.FingerPosition;
import inc.flide.vim8.models.KeyboardAction;
import inc.flide.vim8.models.KeyboardActionType;
import inc.flide.vim8.models.KeyboardData;
import inc.flide.vim8.models.KeyboardDataKt;
import inc.flide.vim8.models.LayerLevel;
import inc.flide.vim8.models.MovementSequenceType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainKeypadActionListener extends KeypadActionListener {
    private static final int DELAY_MILLIS_LONG_PRESS_CONTINUATION = 50;
    private static final int DELAY_MILLIS_LONG_PRESS_INITIATION = 500;
    private static final Set<List<Integer>> extraLayerMovementSequences =
            new HashSet<>(LayerLevel.getMovementSequences().values());
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
            longPressHandler.postDelayed(this, DELAY_MILLIS_LONG_PRESS_CONTINUATION);
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

    public static void rebuildKeyboardData(KeyboardData keyboardData) {
        MainKeypadActionListener.keyboardData = keyboardData;
    }

    public static int getSectors() {
        return keyboardData.getSectors();
    }

    public static KeyboardData getKeyboardData() {
        return keyboardData;
    }

    public static int getLayoutPositions() {
        return keyboardData.getLayoutPositions();
    }

    public String getLowerCaseCharacters(LayerLevel layer) {
        return KeyboardDataKt.lowerCaseCharacters(keyboardData, layer).getOrNull();
    }

    public String getUpperCaseCharacters(LayerLevel layer) {
        return KeyboardDataKt.upperCaseCharacters(keyboardData, layer).getOrNull();
    }

    public String getCurrentLetter() {
        return currentLetter;
    }

    @Override
    public LayerLevel findLayer() {
        for (int i = LayerLevel.getVisibleLayers().size() - 1; i >= LayerLevel.SECOND.ordinal(); i--) {
            LayerLevel layerLevel = LayerLevel.values()[i];
            List<Integer> extraLayerMovementSequence = LayerLevel.getMovementSequences().get(layerLevel);
            if (extraLayerMovementSequence == null) {
                return LayerLevel.FIRST;
            }
            if (movementSequence.size() < extraLayerMovementSequence.size()) {
                continue;
            }

            List<Integer> startWith = movementSequence.subList(0, extraLayerMovementSequence.size());
            if (extraLayerMovementSequences.contains(startWith)
                    && layerLevel.ordinal() <= keyboardData.getTotalLayers()) {
                return layerLevel;
            }
        }
        List<Integer> tempMovementSequence = new ArrayList<>(movementSequence);
        tempMovementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return KeyboardDataKt.findLayer(keyboardData, tempMovementSequence);
    }

    private int getFullRotationSteps() {
        // center, first sector, full rotation and next step
        return 1 + 1 + keyboardData.getSectors() + 1;
    }

    private boolean isFullRotation() {
        LayerLevel layer = findLayer();
        int size = getFullRotationSteps();
        int start = 1;
        boolean layerCondition = movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;

        if (layer.ordinal() > LayerLevel.FIRST.ordinal()) {
            List<Integer> extraLayerMovementSequence = LayerLevel.getMovementSequences().get(layer);

            if (extraLayerMovementSequence != null) {
                size += extraLayerMovementSequence.size();
                start += extraLayerMovementSequence.size();
                layerCondition = extraLayerMovementSequences.contains(
                        movementSequence.subList(0, extraLayerMovementSequence.size()));
            }
        }
        if (movementSequence.size() == size && layerCondition) {
            boolean res = true;
            int direction = FingerPosition.getDirection(
                    movementSequence.get(start),
                    movementSequence.get(start + 1),
                    keyboardData.getSectors());
            for (int i = 1; i < size && start + i + 1 < movementSequence.size(); i++) {
                if ((movementSequence.get(start + i) - 1 + direction)
                        % keyboardData.getSectors() != movementSequence.get(start + i + 1) - 1) {
                    return false;
                }
            }
            return true;
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
                int size = getFullRotationSteps() - 1;
                LayerLevel layer = findLayer();
                if (layer != LayerLevel.FIRST) {
                    List<Integer> extraLayerMovementSequence = LayerLevel.getMovementSequences().get(layer);
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
                LayerLevel layer = findLayer();
                List<Integer> extraLayerMovementSequences = new ArrayList<>();
                int layerSize = 0;
                if (layer != LayerLevel.FIRST) {
                    extraLayerMovementSequences = LayerLevel.Companion.getMovementSequences().get(layer);
                    layerSize = extraLayerMovementSequences.size() + 1;
                }
                boolean defaultLayerCondition = layer == LayerLevel.FIRST
                        && movementSequence.get(0) == FingerPosition.INSIDE_CIRCLE;
                boolean extraLayerCondition = layer != LayerLevel.FIRST && movementSequence.size() > layerSize;
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
        longPressHandler.postDelayed(longPressRunnable, DELAY_MILLIS_LONG_PRESS_INITIATION);
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
}
