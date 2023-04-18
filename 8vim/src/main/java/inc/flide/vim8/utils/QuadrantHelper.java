package inc.flide.vim8.utils;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.Quadrant;

public final class QuadrantHelper {
    private QuadrantHelper() {

    }

    public static List<FingerPosition> computeMovementSequence(int layer, Quadrant quadrant, int position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (quadrant == Quadrant.NO_SECTOR) {
            return movementSequence;
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);

        for (int i = 0; i <= position + 1; i++) {
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

    private static Quadrant getOppositeQuadrant(Quadrant quadrant, int position) {
        Quadrant oppositeQuadrant = quadrant;
        switch (position) {
            case 0:
                switch (quadrant) {
                    case RIGHT_BOTTOM:
                        oppositeQuadrant = Quadrant.RIGHT_TOP;
                        break;
                    case BOTTOM_RIGHT:
                        oppositeQuadrant = Quadrant.BOTTOM_LEFT;
                        break;
                    case BOTTOM_LEFT:
                        oppositeQuadrant = Quadrant.BOTTOM_RIGHT;
                        break;
                    case LEFT_BOTTOM:
                        oppositeQuadrant = Quadrant.LEFT_TOP;
                        break;
                    case LEFT_TOP:
                        oppositeQuadrant = Quadrant.LEFT_BOTTOM;
                        break;
                    case TOP_LEFT:
                        oppositeQuadrant = Quadrant.TOP_RIGHT;
                        break;
                    case TOP_RIGHT:
                        oppositeQuadrant = Quadrant.TOP_LEFT;
                        break;
                    case RIGHT_TOP:
                        oppositeQuadrant = Quadrant.RIGHT_BOTTOM;
                        break;
                }
                break;
            case 1:
                switch (quadrant) {
                    case RIGHT_BOTTOM:
                        oppositeQuadrant = Quadrant.BOTTOM_RIGHT;
                        break;
                    case BOTTOM_RIGHT:
                        oppositeQuadrant = Quadrant.RIGHT_BOTTOM;
                        break;
                    case BOTTOM_LEFT:
                        oppositeQuadrant = Quadrant.LEFT_BOTTOM;
                        break;
                    case LEFT_BOTTOM:
                        oppositeQuadrant = Quadrant.BOTTOM_LEFT;
                        break;
                    case LEFT_TOP:
                        oppositeQuadrant = Quadrant.TOP_LEFT;
                        break;
                    case TOP_LEFT:
                        oppositeQuadrant = Quadrant.LEFT_TOP;
                        break;
                    case TOP_RIGHT:
                        oppositeQuadrant = Quadrant.RIGHT_TOP;
                        break;
                    case RIGHT_TOP:
                        oppositeQuadrant = Quadrant.TOP_RIGHT;
                        break;
                }
                break;
            case 2:
                switch (quadrant) {
                    case RIGHT_BOTTOM:
                        oppositeQuadrant = Quadrant.LEFT_BOTTOM;
                        break;
                    case BOTTOM_RIGHT:
                        oppositeQuadrant = Quadrant.TOP_RIGHT;
                        break;
                    case BOTTOM_LEFT:
                        oppositeQuadrant = Quadrant.TOP_LEFT;
                        break;
                    case LEFT_BOTTOM:
                        oppositeQuadrant = Quadrant.RIGHT_BOTTOM;
                        break;
                    case LEFT_TOP:
                        oppositeQuadrant = Quadrant.RIGHT_TOP;
                        break;
                    case TOP_LEFT:
                        oppositeQuadrant = Quadrant.BOTTOM_LEFT;
                        break;
                    case TOP_RIGHT:
                        oppositeQuadrant = Quadrant.BOTTOM_RIGHT;
                        break;
                    case RIGHT_TOP:
                        oppositeQuadrant = Quadrant.LEFT_TOP;
                        break;
                }
                break;
            case 3:
                switch (quadrant) {
                    case RIGHT_BOTTOM:
                        oppositeQuadrant = Quadrant.TOP_LEFT;
                        break;
                    case BOTTOM_RIGHT:
                        oppositeQuadrant = Quadrant.LEFT_TOP;
                        break;
                    case BOTTOM_LEFT:
                        oppositeQuadrant = Quadrant.RIGHT_TOP;
                        break;
                    case LEFT_BOTTOM:
                        oppositeQuadrant = Quadrant.TOP_RIGHT;
                        break;
                    case LEFT_TOP:
                        oppositeQuadrant = Quadrant.BOTTOM_RIGHT;
                        break;
                    case TOP_LEFT:
                        oppositeQuadrant = Quadrant.RIGHT_BOTTOM;
                        break;
                    case TOP_RIGHT:
                        oppositeQuadrant = Quadrant.LEFT_BOTTOM;
                        break;
                    case RIGHT_TOP:
                        oppositeQuadrant = Quadrant.BOTTOM_LEFT;
                        break;
                }
                break;
        }

        return oppositeQuadrant;
    }

    private static FingerPosition getNextPosition(Quadrant quadrant, FingerPosition lastPosition) {
        if (lastPosition == FingerPosition.NO_TOUCH
            || lastPosition == FingerPosition.LONG_PRESS
            || lastPosition == FingerPosition.LONG_PRESS_END) {
            return null;
        }

        FingerPosition nextPosition = null;
        switch (quadrant) {
            case RIGHT_BOTTOM:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case TOP:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.TOP;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                }
                break;
            case BOTTOM_RIGHT:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case LEFT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.TOP;
                        break;
                }
                break;
            case BOTTOM_LEFT:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case RIGHT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.TOP;
                        break;
                }
                break;
            case LEFT_BOTTOM:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case TOP:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.TOP;
                        break;
                }
                break;
            case LEFT_TOP:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case BOTTOM:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.TOP;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                }
                break;
            case TOP_LEFT:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case RIGHT:
                        nextPosition = FingerPosition.TOP;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.LEFT;
                        break;
                }
                break;
            case TOP_RIGHT:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case LEFT:
                        nextPosition = FingerPosition.TOP;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case BOTTOM:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                }
                break;
            case RIGHT_TOP:
                switch (lastPosition) {
                    case INSIDE_CIRCLE:
                    case BOTTOM:
                        nextPosition = FingerPosition.RIGHT;
                        break;
                    case LEFT:
                        nextPosition = FingerPosition.BOTTOM;
                        break;
                    case TOP:
                        nextPosition = FingerPosition.LEFT;
                        break;
                    case RIGHT:
                        nextPosition = FingerPosition.TOP;
                        break;
                }
                break;
            case NO_SECTOR:
                break;
        }
        return nextPosition;
    }

}
