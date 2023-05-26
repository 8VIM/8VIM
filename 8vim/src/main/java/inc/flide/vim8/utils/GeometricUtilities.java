package inc.flide.vim8.utils;

import android.graphics.PointF;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;

public final class GeometricUtilities {

    private GeometricUtilities() {
    }

    public static double getSquaredDistanceBetweenPoints(PointF a, PointF b) {
        double xSquare = Math.pow(a.x - b.x, 2);
        double ySquare = Math.pow(a.y - b.y, 2);
        return Math.abs(xSquare + ySquare);
    }

    public static Direction getBaseQuadrant(int continuousQuadrantValue) {
        int result;
        // Calculate result with modulus operator
        result = continuousQuadrantValue % Constants.NUMBER_OF_SECTORS;
        return Direction.values()[result];
    }
}

