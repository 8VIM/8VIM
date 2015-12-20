package inc.flide.eightvim.geometry;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by flide on 17/12/15.
 */
public class LineSegment {
    PointF a;
    PointF b;

    public PointF getB() {
        return b;
    }

    public PointF getA() {
        return a;
    }

    public LineSegment(PointF a, PointF b){
        this.a = a;
        this.b = b;
    }

    public LineSegment(PointF startingPoint, int directionalAngleInDegree, int length){

        double directionalAngleInRadians = Math.toRadians(directionalAngleInDegree);
        int x = (int) (startingPoint.x + (length * Math.cos(directionalAngleInRadians)));
        int y = (int) (startingPoint.y + (length * Math.sin(directionalAngleInRadians)));
        PointF endPoint = new PointF(x,y);

        this.a = startingPoint;
        this.b = endPoint;
    }
}
