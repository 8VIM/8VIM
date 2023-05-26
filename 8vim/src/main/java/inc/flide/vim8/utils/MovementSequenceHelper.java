package inc.flide.vim8.utils;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.Quadrant;

public final class MovementSequenceHelper {
    private MovementSequenceHelper() {
    }

    public static List<FingerPosition> computeMovementSequence(int layer, Quadrant quadrant, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }

        List<FingerPosition> movementSequencesForDefaultLayer = movementSequencesForDefaultLayer(quadrant, position);
        if (movementSequencesForDefaultLayer.isEmpty()) {
            return movementSequencesForDefaultLayer;
        } else {
            movementSequence.add(FingerPosition.INSIDE_CIRCLE);
            movementSequence.addAll(movementSequencesForDefaultLayer);
        }

        List<FingerPosition> movementSequenceForExtraLayer = movementSequenceForExtraLayer(layer, quadrant, position);
        movementSequence.addAll(movementSequenceForExtraLayer);
        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static List<FingerPosition> movementSequencesForDefaultLayer(Quadrant quadrant, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();
        int maxMovements = position.ordinal() + 1;
        for (int i = 0; i <= maxMovements; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);
            FingerPosition nextPosition = getNextPosition(quadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static List<FingerPosition> movementSequenceForExtraLayer(int layer, Quadrant quadrant,
                                                                      CharacterPosition position) {
        Quadrant oppositeQuadrant = quadrant.getOppositeQuadrant(position);
        List<FingerPosition> movementSequence = new ArrayList<>();

        for (int i = Constants.DEFAULT_LAYER + 1; i <= layer; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);

            FingerPosition nextPosition = getNextPosition(oppositeQuadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static FingerPosition getNextPosition(Quadrant quadrant, FingerPosition lastPosition) {
        FingerPosition currentSector = Direction.toFingerPosition(quadrant.getSector());
        FingerPosition oppositeSector = Direction.toFingerPosition(Direction.getOpposite(quadrant.getSector()));
        FingerPosition currentPart = Direction.toFingerPosition(quadrant.getPart());
        FingerPosition oppositePart = Direction.toFingerPosition(Direction.getOpposite(quadrant.getPart()));

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
