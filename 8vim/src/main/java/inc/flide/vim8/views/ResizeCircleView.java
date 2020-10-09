package inc.flide.vim8.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ResizeCircleView extends View {

    private int radius;

    public ResizeCircleView(Context context) {
        super(context);
    }

    public ResizeCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ResizeCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generat
        //  ed method stub
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        // RectF rect = new RectF(100, 100, 200, 200);
        // canvas.drawRect(rect, paint);
        canvas.drawCircle(50, 50, getRadius(), paint);
        canvas.drawLine(50, 50, 50, 10 + 15, paint);

    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * @return the radius
     */
    public int getRadius() {
        return radius;
    }
}
