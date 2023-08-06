package inc.flide.vim8.utils;

import android.graphics.PointF;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;

public final class GeometricUtilities {
    private GeometricUtilities() {
    }

    public static double getSquaredDistanceBetweenPoints(PointF a, PointF b) {
        double distanceXSquare = Math.pow(a.x - b.x, 2);
        double distanceYSquare = Math.pow(a.y - b.y, 2);
        return Math.abs(distanceXSquare + distanceYSquare);
    }
}

