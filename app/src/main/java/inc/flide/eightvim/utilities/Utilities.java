package inc.flide.eightvim.utilities;

import android.graphics.PointF;

import java.util.List;

public class Utilities {

    public static float[] convertPointFListToPrimitiveFloatArray(List<PointF> FloatList) {
        float[] floatArray = new float[FloatList.size()*2];
        for(int i=0, j=0; i<FloatList.size(); i++){
            floatArray[j++] = FloatList.get(i).x;
            floatArray[j++] = FloatList.get(i).y;
        }
        return floatArray;
    }
}
