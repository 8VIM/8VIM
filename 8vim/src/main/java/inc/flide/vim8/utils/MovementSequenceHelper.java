package inc.flide.vim8.utils;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.yaml.ExtraLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MovementSequenceHelper {
    private MovementSequenceHelper() {
    }

    public static List<Integer> computeMovementSequence(int layer, Quadrant quadrant,
                                                               CharacterPosition position) {
        List<Integer> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }

        List<Integer> movementSequencesForDefaultLayer = movementSequencesForLayer(layer, quadrant, position);
        if (movementSequencesForDefaultLayer.isEmpty()) {
            return movementSequencesForDefaultLayer;
        } else {
            movementSequence.addAll(movementSequencesForDefaultLayer);
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    public static List<Integer> computeQuickMovementSequence(int layer, Quadrant quadrant,
                                                                    CharacterPosition position) {
        if (layer <= Constants.DEFAULT_LAYER) {
            return Collections.emptyList();
        }
        List<Integer> movementSequence = new ArrayList<>();

        List<Integer> movementSequenceForExtraLayer = movementSequenceForExtraLayer(layer, quadrant, position);
        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        movementSequence.addAll(movementSequenceForExtraLayer);
        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static List<Integer> movementSequenceForExtraLayer(int layer, Quadrant quadrant,
                                                                      CharacterPosition position) {
        Quadrant oppositeQuadrant = quadrant.getOppositeQuadrant(position);
        List<Integer> movementSequence = new ArrayList<>();

        int maxMovements = position.ordinal() + 1;
        for (int i = 0; i <= maxMovements; i++) {
            int lastPosition =
                    movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE :
                            movementSequence.get(movementSequence.size() - 1);
            int nextPosition = getNextPosition(quadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        for (int i = Constants.DEFAULT_LAYER + 1; i <= layer; i++) {
            int lastPosition =
                    movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE :
                            movementSequence.get(movementSequence.size() - 1);

            int nextPosition = getNextPosition(oppositeQuadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static List<Integer> movementSequencesForLayer(int layer, Quadrant quadrant,
                                                                  CharacterPosition position) {
        List<Integer> movementSequence = new ArrayList<>();
        int maxMovements = position.ordinal() + 1;

        if (layer > Constants.DEFAULT_LAYER) {
            ExtraLayer extraLayer = ExtraLayer.values()[layer - 2];
            List<Integer> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
            if (extraLayerMovementSequence != null) {
                movementSequence.addAll(extraLayerMovementSequence);
            }
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);

        for (int i = 0; i <= maxMovements; i++) {
            Integer lastPosition = movementSequence.get(movementSequence.size() - 1);
            Integer nextPosition = getNextPosition(quadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    // TODO: To fix since it dow not work with 6 sectors
    private static int getNextPosition(Quadrant quadrant, int lastPosition) {
        int currentSector = Direction.toFingerPosition(quadrant.getSector());
        int oppositeSector = Direction.toFingerPosition(Direction.getOpposite(quadrant.getSector()));
        int currentPart = Direction.toFingerPosition(quadrant.getPart());
        int oppositePart = Direction.toFingerPosition(Direction.getOpposite(quadrant.getPart()));

        if (lastPosition == FingerPosition.INSIDE_CIRCLE || lastPosition == oppositePart) {
            return currentSector;
        } else if (lastPosition == oppositeSector) {
            return oppositePart;
        } else if (lastPosition == currentPart) {
            return oppositeSector;
        } else {
            return currentPart;
        }
    }
}
