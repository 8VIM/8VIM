package inc.flide.eightvim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import inc.flide.eightvim.geometry.Circle;
import inc.flide.eightvim.geometry.GeometryUtilities;
import inc.flide.logging.Logger;

import static inc.flide.eightvim.geometry.GeometryUtilities.*;

public class EightVimKeyboardView extends View{

    enum FingerPosition {NO_TOUCH, INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT};

    private EightVimInputMethodService eightVimInputMethodService;

    private List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private Circle circle;

    private float		radius;						// Radius of circle
    private PointF		centre;

    private char[][]	characters = {
            {'a', 's', 'i', 'o'},
            {'r', 'd', 'h', 'u'},
            {'x', 'g', 'j', 'v'},
            {'?', '\'', ',', 'w'},
            {'n', 'y', 't', 'e'},
            {'m', 'b', 'c', 'l'},
            {'f', 'p', 'z', 'k'},
            {'!', 'q', '.', '@'}};	// Table of the arranged characters

    public EightVimKeyboardView(Context context) {
        super(context);
        initialize(context);
    }

    public EightVimKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public EightVimKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context){
        eightVimInputMethodService = (EightVimInputMethodService) context;
        setHapticFeedbackEnabled(true);
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }


    @Override
    public void onDraw(Canvas canvas)
    {
        Logger.v(this, "onDraw called");
        //super.onDraw(canvas);
        canvas.drawColor(0);
        Paint paint = new Paint();
        paint.setARGB(255,255,255,255);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(paint.getColor());
        paint = new Paint();
        paint.setARGB(255,0,0,0);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        //create the centre circle
        RectF oval = new RectF();
        oval.set(centre.x-radius, centre.y-radius, centre.x+radius, centre.y+radius);
        canvas.drawArc(oval, 0f, 360f, false, paint);
        canvas.drawCircle(centre.x, centre.y, radius,paint);

        float r = radius + 200;
        canvas.drawLine(centre.x, centre.y, centre.x-r, centre.y-r, paint);
        canvas.drawLine(centre.x, centre.y, centre.x-r, centre.y+r, paint);
        canvas.drawLine(centre.x, centre.y, centre.x + r, centre.y + r, paint);
        canvas.drawLine(centre.x, centre.y, centre.x + r, centre.y - r, paint);
        Logger.v(this, "onDraw returns");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Logger.v(this, "onMeasure called");
        // Get size without mode
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        // Get orientation
        if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
        {
            // Switch to button mode
            // TODO
            width = Math.round(1.33f * height);
            // Placeholder:
        }
        else  // Portrait mode
        {
            // Adjust the height to match the aspect ratio 4:3
            height = Math.round(0.75f * width);
        }

        // Calculate the diameter with the circle width to image width ratio 260:800,
        // and divide in half to get the radius
        radius = (0.325f * width) / 2;
        centre = new PointF((width/2),(height/2));
        circle = new Circle(centre, radius);
        // Set the new size
        setMeasuredDimension(width, height);
        Logger.v(this, "onMeasure returns");
    }




    /** Modulus calculation (a % b) that supports negative numbers */
    private double mod(double a, double b)
    {
        double result;
        // Calculate result with modulus operator
        result = a % b;
        // Fix zero truncation
        if(result < 0){
            result += b;
        }
        return result;
    }

    /** Get the number of the sector that point p is in
     *  @return 0: right, 1: top, 2: left, 3: bottom */
    private FingerPosition getSector(PointF p)
    {
        double angleDouble = GeometryUtilities.getAngleOfPointWithRespectToCentreOfCircle(p, circle);
        double angleToSectorValue = angleDouble/ (Math.PI / 2);
        int quadrantCyclic = (int)Math.round(angleToSectorValue);
        int numericSector = (int)mod(quadrantCyclic, 4);

        switch (numericSector){
            case 0:
                return FingerPosition.RIGHT;

            case 1:
                return FingerPosition.TOP;

            case 2 :
                return FingerPosition.LEFT;

            case 3 :
                return FingerPosition.BOTTOM;

        }
        return null;
    }



    private FingerPosition getCurrentFingerPosition(PointF position) {
        double powerOfPoint = getPowerOfPoint(position, circle);
        if(powerOfPoint < 0){
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return getSector(position);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        switch(e.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                img_Input_onTouchDown(e);
                break;

            case MotionEvent.ACTION_MOVE:
                img_Input_onTouchMove(e);
                break;

            case MotionEvent.ACTION_UP:
                img_Input_onTouchUp(e);
                break;

            default:
                return false;
        }

        return true;
    }

    private void img_Input_onTouchDown(MotionEvent e)
    {
        PointF position = new PointF(e.getX(), e.getY());
        currentFingerPosition = getCurrentFingerPosition(position);
        movementSequence.clear();
        movementSequence.add(currentFingerPosition);
    }

    private void img_Input_onTouchMove(MotionEvent e)
    {
        PointF position = new PointF(e.getX(), e.getY());
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = getCurrentFingerPosition(position);

        boolean isFingerPositionChanged = (lastKnownFingerPosition == currentFingerPosition);

        if(isFingerPositionChanged){
            movementSequence.add(currentFingerPosition);
            if(currentFingerPosition == FingerPosition.INSIDE_CIRCLE){
                processMovementSequence();
            }
        }
    }

    private void processMovementSequence() {
        //TODO
        //Clear the queue before this function finishes off
    }

    private void img_Input_onTouchUp(MotionEvent e)
    {
        currentFingerPosition = FingerPosition.NO_TOUCH;
        movementSequence.add(currentFingerPosition);
        processMovementSequence();
    }

}
