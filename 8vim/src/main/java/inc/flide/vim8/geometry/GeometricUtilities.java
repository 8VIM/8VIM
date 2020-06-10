package inc.flide.vim8.geometry;

import android.graphics.PointF;

public class GeometricUtilities {

    public static double getSquaredDistanceBetweenPoints(PointF a, PointF b){
        double xSquare = Math.pow((a.x - b.x),2);
        double ySquare = Math.pow((a.y - b.y), 2);
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

    public static PointF findPointSpecifiedDistanceAwayInGivenDirection(PointF startingPoint, double directionalAngleInDegree, double length){
        double directionalAngleInRadians = Math.toRadians(directionalAngleInDegree);
        int x = (int) (startingPoint.x + (length * Math.cos(directionalAngleInRadians)));
        int y = (int) (startingPoint.y + (length * Math.sin(directionalAngleInRadians)));
        PointF endPoint = new PointF(x,y);
        return endPoint;
    }

}

