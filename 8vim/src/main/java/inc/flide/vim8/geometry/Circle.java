package inc.flide.vim8.geometry;

import android.graphics.PointF;

import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.utils.GeometricUtilities;

public class Circle {
    private PointF centre;
    private float radius;

    public Circle() {
        this.centre = new PointF(0f, 0f);
        this.radius = 0f;
    }

    public PointF getCentre() {
        return centre;
    }

    public void setCentre(PointF centre) {
        this.centre = centre;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    private double getPowerOfPoint(PointF point) {
        /*
        If O is the centre of circle
        Consider startingPoint point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O,
        then the power of the point P relative to the circle is
        p=d^2-r^2.
        */

        double squaredDistanceBetweenPoints = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre);
        double radiusSquare = Math.pow(radius, 2);
        return squaredDistanceBetweenPoints - radiusSquare;
    }

    public boolean isPointInsideCircle(PointF point) {
        return getPowerOfPoint(point) < 0;
    }

    /**
     * Gets the angle of point p relative to the center
     */
    private double getAngleInRadiansOfPointWithRespectToCentreOfCircle(PointF point) {
        // Get difference of coordinates
        double x = point.x - centre.x;
        double y = centre.y - point.y;

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        double angle = Math.atan2(y, x);
        // Make all angles positive
        if (angle < 0) {
            angle = Math.PI * 2 + angle;
        }
        return angle;
    }

    /**
     * Get the number of the sector that point p is in
     */
    public FingerPosition getSectorOfPoint(PointF p) {
        double angleDouble = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p);
        double angleToSectorValue = angleDouble / (Math.PI / 2);
        int quadrantCyclic = (int) Math.round(angleToSectorValue);
        Direction baseQuadrant = GeometricUtilities.getBaseQuadrant(quadrantCyclic);
        FingerPosition result = null;

        switch (baseQuadrant) {
            case RIGHT:
                return FingerPosition.RIGHT;
            case TOP:
                return FingerPosition.TOP;
            case LEFT:
                return FingerPosition.LEFT;
            case BOTTOM:
                return FingerPosition.BOTTOM;
            default:
                return null;
        }
    }
}
