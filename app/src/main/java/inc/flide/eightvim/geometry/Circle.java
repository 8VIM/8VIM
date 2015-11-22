package inc.flide.eightvim.geometry;

import android.graphics.PointF;

/**
 * Created by flide on 22/11/15.
 */
public class Circle{
    private PointF centre;
    private double radius;

    public Circle(PointF centre, double radius) {
        this.centre = centre;
        this.radius = radius;
    }

    public PointF getCentre() {
        return centre;
    }

    public void setCentre(PointF centre) {
        this.centre = centre;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
