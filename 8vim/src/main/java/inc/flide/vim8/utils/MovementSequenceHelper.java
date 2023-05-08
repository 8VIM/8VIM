package inc.flide.vim8.utils;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.SectorPart;

public final class MovementSequenceHelper {
    private MovementSequenceHelper() {
    }

    public static List<FingerPosition> computeMovementSequence(int layer, SectorPart sector, SectorPart part, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }


        List<FingerPosition> movementSequencesForDefaultLayer = movementSequencesForDefaultLayer(sector, part, position);
        if (movementSequencesForDefaultLayer.isEmpty()) {
            return movementSequencesForDefaultLayer;
        } else {
            movementSequence.add(FingerPosition.INSIDE_CIRCLE);
            movementSequence.addAll(movementSequencesForDefaultLayer);
        }

        List<FingerPosition> movementSequenceForExtraLayer = movementSequenceForExtraLayer(layer, sector, part, position);
        if (movementSequenceForExtraLayer == null) {
            return new ArrayList<>();
        } else {
            movementSequence.addAll(movementSequenceForExtraLayer);
        }
        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static List<FingerPosition> movementSequencesForDefaultLayer(SectorPart sector, SectorPart part, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();
        int maxMovements = position.ordinal() + 1;
        for (int i = 0; i <= maxMovements; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);
            FingerPosition nextPosition = getNextPosition(sector, part, lastPosition);
            if (nextPosition == null) {
                return new ArrayList<>();
            }
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static List<FingerPosition> movementSequenceForExtraLayer(int layer, SectorPart sector, SectorPart part, CharacterPosition position) {
        SectorPart[] oppositeSectorParts = SectorPart.getOppositeSectorPart(sector, part, position);
        List<FingerPosition> movementSequence = new ArrayList<>();

        for (int i = Constants.DEFAULT_LAYER + 1; i <= layer; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);

            FingerPosition nextPosition = getNextPosition(oppositeSectorParts[0], oppositeSectorParts[1], lastPosition);
            if (nextPosition == null) {
                return null;
            }
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static FingerPosition getNextPosition(SectorPart sector, SectorPart part, FingerPosition lastPosition) {
        if (!FingerPosition.VALID_FINGER_POSITIONS.contains(lastPosition)) {
            return null;
        }

        FingerPosition currentSector = SectorPart.toFingerPosition(sector);
        FingerPosition oppositeSector = SectorPart.toFingerPosition(SectorPart.getOpposite(sector));
        FingerPosition currentPart = SectorPart.toFingerPosition(part);
        FingerPosition oppositePart = SectorPart.toFingerPosition(SectorPart.getOpposite(part));

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
