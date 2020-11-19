package inc.flide.vim8.geometry;

import android.graphics.PointF;

public class LineSegment {
    private PointF startingPoint;
    private PointF endPoint;
    private double length;
    private double directionOfLineInDegree;

    public LineSegment(PointF startingPoint, PointF endPoint) {
        this.startingPoint = startingPoint;
        this.endPoint = endPoint;
        this.length = computeLengthOfLineSegment();
        this.directionOfLineInDegree = computeAngleOfLineSegment();
    }

    public LineSegment() {
        this.startingPoint = new PointF();
        this.endPoint = new PointF();
        this.length = 0;
        this.directionOfLineInDegree = 0;
    }

    public LineSegment(PointF startingPoint, double directionalAngleInDegree, double length) {
        setupLineSegment(startingPoint, directionalAngleInDegree, length);
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getDirectionOfLineInDegree() {
        return directionOfLineInDegree;
    }

    public void setDirectionOfLineInDegree(double directionOfLineInDegree) {
        this.directionOfLineInDegree = directionOfLineInDegree;
    }

    public PointF getEndPoint() {
        return endPoint;
    }

    public PointF getStartingPoint() {
        return startingPoint;
    }

    private double computeAngleOfLineSegment() {
        double slope = (startingPoint.y - endPoint.y) / (startingPoint.x - endPoint.x);
        double angleOfDirectionInRadians = Math.atan(slope);
        return Math.toDegrees(angleOfDirectionInRadians);
    }

    private double computeLengthOfLineSegment() {
        return Math.sqrt(GeometricUtilities.getSquaredDistanceBetweenPoints(startingPoint, endPoint));
    }

    public void setupLineSegment(PointF startingPoint, double directionalAngleInDegree, double length) {
        this.startingPoint = startingPoint;
        this.endPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(startingPoint, directionalAngleInDegree, length);
        this.length = length;
        this.directionOfLineInDegree = directionalAngleInDegree;
    }

    public boolean isSlopePositive() {
        double slope = (startingPoint.y - endPoint.y) / (startingPoint.x - endPoint.x);
        return (slope >= 0);
    }
}
