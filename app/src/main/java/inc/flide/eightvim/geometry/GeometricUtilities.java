package inc.flide.eightvim.geometry;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.List;

/**
 * Created by flide on 22/11/15.
 */
public class GeometricUtilities {

    public static double getSquaredDistanceBetweenPoints(PointF a, PointF b){
        double xSquare = Math.pow((a.x - b.x),2);
        double ySquare = Math.pow((a.y - b.y),2);
        double distanceSquare = Math.abs(xSquare+ySquare);
        return distanceSquare;
    }

    public static int getBaseQuadrant(int continiousQuadrantValue)
    {
        int result;
        // Calculate result with modulus operator
        result = continiousQuadrantValue % 4;
        // Fix zero truncation
        if(result < 0){
            result += 4;
        }
        return result;
    }

}
