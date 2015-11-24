package inc.flide.eightvim.geometry;

import android.graphics.PointF;

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



    /** Gets the angle of point p relative to the center */
    public static double getAngleOfPointWithRespectToCentreOfCircle(PointF p, Circle circle)
    {
        // Get difference of coordinates
        double x = p.x - circle.getCentre().x;
        double y = circle.getCentre().y - p.y;

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        double angle = Math.atan2(y, x);
        // Make all angles positive
        if(angle < 0) {
            angle = Math.PI * 2 + angle;
        }
        return angle;
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
