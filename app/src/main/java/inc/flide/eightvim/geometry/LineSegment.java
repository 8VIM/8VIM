package inc.flide.eightvim.geometry;

import android.graphics.PointF;

public class LineSegment {
    private PointF startingPoint;
    private PointF endPoint;
    private double length;
    private double directionOfLineInDegree;

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

    public LineSegment(PointF startingPoint, PointF endPoint){
        this.startingPoint = startingPoint;
        this.endPoint = endPoint;
        this.length = computeLengthOfLineSegment();
        this.directionOfLineInDegree = computeAngleOfLineSegment();
    }

    private double computeAngleOfLineSegment() {
        double slope = (startingPoint.y - endPoint.y)/(startingPoint.x - endPoint.x);
        double angleOfDirectionInRadians = Math.atan(slope);
        return Math.toDegrees(angleOfDirectionInRadians);
    }

    private double computeLengthOfLineSegment() {
        return Math.sqrt(GeometricUtilities.getSquaredDistanceBetweenPoints(startingPoint, endPoint));
    }

    public LineSegment(PointF startingPoint, int directionalAngleInDegree, int length){

        PointF endPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(startingPoint, directionalAngleInDegree, length);

        this.startingPoint = startingPoint;
        this.endPoint = endPoint;
        this.length = length;
        this.directionOfLineInDegree = directionalAngleInDegree;
    }

    public boolean isSlopePositive(){
        double slope = (startingPoint.y - endPoint.y)/(startingPoint.x - endPoint.x);
        return (slope >= 0);
    }
}
