package inc.flide.vim8.utils;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Quadrant;

public final class QuadrantHelper {
    private QuadrantHelper() {
    }

    public static List<FingerPosition> computeMovementSequence(int layer, Quadrant quadrant, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);

        for (int i = 0; i <= position.ordinal() + 1; i++) {
            FingerPosition lastPosition = movementSequence.get(movementSequence.size() - 1);
            FingerPosition nextPosition = getNextPosition(quadrant, lastPosition);
            movementSequence.add(nextPosition);
        }

        Quadrant oppositeQuadrant = getOppositeQuadrant(quadrant, position);

        for (int i = 2; i <= layer; i++) {
            FingerPosition lastPosition;

            if (i == 2) {
                lastPosition = FingerPosition.INSIDE_CIRCLE;
            } else {
                lastPosition = movementSequence.get(movementSequence.size() - 1);
            }

            FingerPosition nextPosition = getNextPosition(oppositeQuadrant, lastPosition);
            movementSequence.add(nextPosition);
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static Quadrant getOppositeQuadrant(Quadrant quadrant, CharacterPosition position) {
        Direction[] quadrantParts = getDirectionsFromQuadrant(quadrant);
        if (quadrantParts == null) {
            return null;
        }
        Direction sector = quadrantParts[0];
        Direction part = quadrantParts[1];

        if (position == CharacterPosition.FIRST) {
            return getQuadrant(sector, Direction.getOpposite(part));
        } else if (position == CharacterPosition.SECOND) {
            return getQuadrant(part, sector);
        } else if (position == CharacterPosition.THIRD) {
            return getQuadrant(Direction.getOpposite(sector), part);
        } else {
            return getQuadrant(Direction.getOpposite(part), Direction.getOpposite(sector));
        }
    }

    private static FingerPosition getNextPosition(Quadrant quadrant, FingerPosition lastPosition) {
        Direction[] sectorPart = getDirectionsFromQuadrant(quadrant);

        if (!FingerPosition.VALID_QUADRANT_POSITIONS.contains(lastPosition) || sectorPart == null) {
            return null;
        }

        Direction sector = sectorPart[0];
        Direction part = sectorPart[1];
        FingerPosition currentSector = Direction.toFingerPosition(sector);
        FingerPosition oppositeSector = Direction.toFingerPosition(Direction.getOpposite(sector));
        FingerPosition currentPart = Direction.toFingerPosition(part);
        FingerPosition oppositePart = Direction.toFingerPosition(Direction.getOpposite(part));

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

    private static Direction[] getDirectionsFromQuadrant(Quadrant quadrant) {
        String[] quadrantSplit = quadrant.name().split("_", 2);
        if (quadrantSplit.length != 2) {
            return null;
        }
        try {
            return new Direction[] {Direction.valueOf(quadrantSplit[0]), Direction.valueOf(quadrantSplit[1])};
        } catch (Exception e) {
            return null;
        }
    }

    public static Quadrant getQuadrant(Direction sector, Direction part) {
        String quadrant = sector.toString() + "_" + part.toString();
        try {
            return Quadrant.valueOf(quadrant);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
