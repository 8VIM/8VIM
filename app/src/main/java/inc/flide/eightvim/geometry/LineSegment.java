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
}
