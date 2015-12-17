package inc.flide.eightvim.geometry;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by flide on 22/11/15.
 */
public class Circle{
    private Point centre;
    private float radius;

    public Circle(Point centre, float radius) {
        this.centre = centre;
        this.radius = radius;
    }

    public Point getCentre() {
        return centre;
    }

    public void setCentre(Point centre) {
        this.centre = centre;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    private double getPowerOfPoint(Point point) {
        /*
        If O is the centre of circle
        Consider a point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */

        double dSquare = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre);
        double rSquare = Math.pow(radius, 2);
        double power = dSquare - rSquare;
        return power;
    }

    public boolean isPointInsideCircle(Point point){
        if(getPowerOfPoint(point) < 0){
            return true;
        }
        return false;
    }

}
