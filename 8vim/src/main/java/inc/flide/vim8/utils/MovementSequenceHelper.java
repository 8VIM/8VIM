package inc.flide.vim8.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.SectorPart;

public final class MovementSequenceHelper {
    private MovementSequenceHelper() {
    }

    public static List<FingerPosition> computeMovementSequence(int layer, Pair<SectorPart, SectorPart> sectorParts, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }

        List<FingerPosition> movementSequencesForDefaultLayer = movementSequencesForDefaultLayer(sectorParts, position);
        if (movementSequencesForDefaultLayer.isEmpty()) {
            return movementSequencesForDefaultLayer;
        } else {
            movementSequence.add(FingerPosition.INSIDE_CIRCLE);
            movementSequence.addAll(movementSequencesForDefaultLayer);
        }

        List<FingerPosition> movementSequenceForExtraLayer = movementSequenceForExtraLayer(layer, sectorParts, position);
        movementSequence.addAll(movementSequenceForExtraLayer);
        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static List<FingerPosition> movementSequencesForDefaultLayer(Pair<SectorPart, SectorPart> sectorParts, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();
        int maxMovements = position.ordinal() + 1;
        for (int i = 0; i <= maxMovements; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);
            FingerPosition nextPosition = getNextPosition(sectorParts, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static List<FingerPosition> movementSequenceForExtraLayer(int layer, Pair<SectorPart, SectorPart> sectorParts,
                                                                      CharacterPosition position) {
        Pair<SectorPart, SectorPart> oppositeSectorParts = SectorPart.getOppositeSectorPart(sectorParts, position);
        List<FingerPosition> movementSequence = new ArrayList<>();

        for (int i = Constants.DEFAULT_LAYER + 1; i <= layer; i++) {
            FingerPosition lastPosition =
                movementSequence.isEmpty() ? FingerPosition.INSIDE_CIRCLE : movementSequence.get(movementSequence.size() - 1);

            FingerPosition nextPosition = getNextPosition(oppositeSectorParts, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static FingerPosition getNextPosition(Pair<SectorPart, SectorPart> sectorParts, FingerPosition lastPosition) {
        FingerPosition currentSector = SectorPart.toFingerPosition(sectorParts.getLeft());
        FingerPosition oppositeSector = SectorPart.toFingerPosition(SectorPart.getOpposite(sectorParts.getLeft()));
        FingerPosition currentPart = SectorPart.toFingerPosition(sectorParts.getRight());
        FingerPosition oppositePart = SectorPart.toFingerPosition(SectorPart.getOpposite(sectorParts.getRight()));

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
