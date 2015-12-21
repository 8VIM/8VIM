package inc.flide.eightvim.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.geometry.Circle;
import inc.flide.eightvim.geometry.GeometricUtilities;
import inc.flide.eightvim.geometry.LineSegment;
import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.utilities.Utilities;
import inc.flide.logging.Logger;

public class EightVimKeyboardView extends View{

    private static final int DELAY_MILLIS_LONG_PRESS_INITIATION = 500;
    private static final int DELAY_MILLIS_LONG_PRESS_CONTINUATION = 50;

    private EightVimInputMethodService eightVimInputMethodService;

    private List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private boolean isLongPressCallbackSet;

    private Circle circle;

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
    public void onDraw(Canvas canvas) {
        Logger.v(this, "onDraw called");

        Paint paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.FILL);

        //background colouring
        canvas.drawColor(paint.getColor());

        paint.setARGB(255, 0, 0, 0);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        //The centre circle
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(), paint);

        //The lines demarcating the sectors
        List<LineSegment> sectorDemarcatingLines = new ArrayList<>();
        int lengthOfLine = 200;
        for(int i =0; i<4 ; i++) {
            int angle = 45+(i*90);
            PointF startingPoint = circle.getPointOnCircumferenceAtDegreeAngle(angle);
            LineSegment lineSegment = new LineSegment(startingPoint, angle, lengthOfLine);
            sectorDemarcatingLines.add(lineSegment);
            canvas.drawLine(lineSegment.getStartingPoint().x, lineSegment.getStartingPoint().y, lineSegment.getEndPoint().x, lineSegment.getEndPoint().y, paint);
        }

        //the text along the lines
        paint.setTextSize(40);
        paint.setStrokeWidth(2);

        String charactersToDisplay = "nmf!elk@tcz.ybpq";
        List<PointF> listOfPointsOfDisplayInAntiClockwiseDirection = new ArrayList<>();
        for(int i=0; i<4; i++) {
            LineSegment currentLine = sectorDemarcatingLines.get(i);
            listOfPointsOfDisplayInAntiClockwiseDirection.addAll(getCharacterDisplayPointsOnTheLineSegmentInAntiClockwiseDirection(currentLine, 4));
        }
        float[] pointsOfDisplay = Utilities.convertPointFListToPrimitiveFloatArray(listOfPointsOfDisplayInAntiClockwiseDirection);
        canvas.drawPosText(charactersToDisplay, pointsOfDisplay, paint);

        Logger.v(this, "onDraw returns");
    }

    private List<PointF> getCharacterDisplayPointsOnTheLineSegmentInAntiClockwiseDirection(LineSegment lineSegment, int numberOfCharactersToDisplay) {
        List<PointF> pointsOfCharacterDisplay = new ArrayList<>();

        //Assuming we got to derive 4 points
        double spacingBetweenPoints = lineSegment.getLength()/numberOfCharactersToDisplay;
        float xOffset = getXOffset(lineSegment);
        float yOffset = getYOffset(lineSegment);

        for(int i = 0 ; i < 4 ; i++){
            PointF nextPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(lineSegment.getStartingPoint(), lineSegment.getDirectionOfLineInDegree(), (spacingBetweenPoints * i));
            PointF nextDisplayPoint = new PointF(nextPoint.x + xOffset, nextPoint.y + yOffset);
            pointsOfCharacterDisplay.add(nextDisplayPoint);
        }

        return pointsOfCharacterDisplay;
    }

    private float getYOffset(LineSegment lineSegment) {
        int reverse = -1;
        int ySign = (lineSegment.getStartingPoint().y - lineSegment.getEndPoint().y)>0?-1:1;
        int slopeSign = lineSegment.isSlopePositive()?1:-1;
        int aggregateSign = ySign*slopeSign*-1;
        float offset = 0;
        if(lineSegment.getDirectionOfLineInDegree()>0 && lineSegment.getDirectionOfLineInDegree() <=90){
            offset = 0;
        }else if(lineSegment.getDirectionOfLineInDegree()>90 && lineSegment.getDirectionOfLineInDegree() <=180){
            offset = 60;
        }else if(lineSegment.getDirectionOfLineInDegree()>180 && lineSegment.getDirectionOfLineInDegree() <=270){
            offset = 20;
        }else{
            offset = 40;
        }
        return offset*aggregateSign;
    }

    private float getXOffset(LineSegment lineSegment) {

        int xSign = (lineSegment.getStartingPoint().x - lineSegment.getEndPoint().x)>0?-1:1;

        int slopeSign = lineSegment.isSlopePositive()?1:-1;
        int aggregateSign = xSign*slopeSign;
        float offset = 0;
        if(lineSegment.getDirectionOfLineInDegree()>0 && lineSegment.getDirectionOfLineInDegree() <=90){
            offset = 30;
        }else if(lineSegment.getDirectionOfLineInDegree()>90 && lineSegment.getDirectionOfLineInDegree() <=180){
            offset = 0;
        }else if(lineSegment.getDirectionOfLineInDegree()>180 && lineSegment.getDirectionOfLineInDegree() <=270){
            offset = 40;
        }else{
            offset = 0;
        }
        return offset*aggregateSign;
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
            Logger.Verbose(this, "Say what you will, but I don't do Landscapes just yet!! So.. BACK OFF!!");
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
        float radius = (0.325f * width) / 2;
        PointF centre = new PointF((width/2),(height/2));
        circle = new Circle(centre, radius);
        // Set the new size
        setMeasuredDimension(width, height);
        Logger.v(this, "onMeasure returns");
    }

    /** Get the number of the sector that point p is in
     *  @return 0: right, 1: top, 2: left, 3: bottom */
    private FingerPosition getSector(PointF p) {
        double angleDouble = circle.getAngleInRadiansOfPointWithRespectToCentreOfCircle(p);
        double angleToSectorValue = angleDouble/ (Math.PI / 2);
        int quadrantCyclic = (int)Math.round(angleToSectorValue);
        int baseQuadrant = GeometricUtilities.getBaseQuadrant(quadrantCyclic);

        switch (baseQuadrant){
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
        if(circle.isPointInsideCircle(position)){
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return getSector(position);
        }
    }

    final Handler longPressHandler = new Handler();
    Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            List<FingerPosition> movementSequenceAgumented = new ArrayList<>(movementSequence);
            movementSequenceAgumented.add(FingerPosition.LONG_PRESS);
            eightVimInputMethodService.processMovementSequence(movementSequenceAgumented);
            longPressHandler.postDelayed(this, DELAY_MILLIS_LONG_PRESS_CONTINUATION);
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                movementStarted(e);
                break;

            case MotionEvent.ACTION_MOVE:
                movementContinues(e);
                break;

            case MotionEvent.ACTION_UP:
                movementEnds(e);
                break;

            default:
                return false;
        }

        return true;
    }

    private void initiateLongPressDetection(){
        isLongPressCallbackSet = true;
        longPressHandler.postDelayed(longPressRunnable, DELAY_MILLIS_LONG_PRESS_INITIATION);
    }

    private void interruptLongPress(){
        longPressHandler.removeCallbacks(longPressRunnable);
        isLongPressCallbackSet = false;
    }

    private void movementStarted(MotionEvent e) {
        PointF position = new PointF((int)e.getX(), (int)e.getY());
        currentFingerPosition = getCurrentFingerPosition(position);
        movementSequence.clear();
        movementSequence.add(currentFingerPosition);
        initiateLongPressDetection();
    }

    private void movementContinues(MotionEvent e) {
        PointF position = new PointF((int)e.getX(), (int)e.getY());
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = getCurrentFingerPosition(position);

        boolean isFingerPositionChanged = (lastKnownFingerPosition != currentFingerPosition);

        if(isFingerPositionChanged){
            interruptLongPress();
            movementSequence.add(currentFingerPosition);
            if(currentFingerPosition == FingerPosition.INSIDE_CIRCLE){
                eightVimInputMethodService.processMovementSequence(movementSequence);
                movementSequence.clear();
                movementSequence.add(currentFingerPosition);
            }
        }else if(!isLongPressCallbackSet){
            initiateLongPressDetection();
        }
    }

    private void movementEnds(MotionEvent e) {
        interruptLongPress();
        currentFingerPosition = FingerPosition.NO_TOUCH;
        movementSequence.add(currentFingerPosition);
        eightVimInputMethodService.processMovementSequence(movementSequence);
        movementSequence.clear();
    }

}
