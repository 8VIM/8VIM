package inc.flide.eightvim.geometry;

import android.graphics.PointF;

/**
 * Created by flide on 22/11/15.
 */
public class GeometryUtilities {

    public static double getSquaredDistanceBetweenPoints(PointF a, PointF b){
        double xSquare = Math.pow((a.x - b.x),2);
        double ySquare = Math.pow((a.y - b.y),2);
        double distanceSquare = Math.abs(xSquare+ySquare);
        return distanceSquare;
    }

    public static double getPowerOfPoint(PointF position, Circle circle) {
        /*
        If O is the centre of circle
        Consider a point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */

        double dSquare = GeometryUtilities.getSquaredDistanceBetweenPoints(position, circle.getCentre());
        double rSquare = Math.pow(circle.getRadius(), 2);
        double power = dSquare - rSquare;
        return power;
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
}
