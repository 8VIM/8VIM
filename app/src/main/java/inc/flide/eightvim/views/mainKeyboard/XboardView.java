package inc.flide.eightvim.views.mainKeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.geometry.Circle;
import inc.flide.eightvim.geometry.GeometricUtilities;
import inc.flide.eightvim.geometry.LineSegment;
import inc.flide.eightvim.keyboardActionListners.MainKeyboardActionListener;
import inc.flide.eightvim.structures.FingerPosition;
import inc.flide.eightvim.utilities.Utilities;

public class XboardView extends View{

    private MainKeyboardActionListener mainKeyboardActionListener;
    private EightVimInputMethodService eightVimInputMethodService;

    private Circle circle;

    public XboardView(Context context) {
        super(context);
        initialize(context);
    }

    public XboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public XboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context){
        eightVimInputMethodService = (EightVimInputMethodService) context;
        mainKeyboardActionListener = new MainKeyboardActionListener(eightVimInputMethodService
                , this);

        setHapticFeedbackEnabled(true);
    }

    @Override
    public void onDraw(Canvas canvas) {

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
        String charactersToDisplay = getCharacterSetToDisplay();
        List<PointF> listOfPointsOfDisplay = new ArrayList<>();
        for(int i=0; i<4; i++) {
            LineSegment currentLine = sectorDemarcatingLines.get(i);
            listOfPointsOfDisplay.addAll(getCharacterDisplayPointsOnTheLineSegment(currentLine, 4));
        }
        float[] pointsOfDisplay = Utilities.convertPointFListToPrimitiveFloatArray(listOfPointsOfDisplay);
        canvas.drawPosText(charactersToDisplay, pointsOfDisplay, paint);

    }

    private String getCharacterSetToDisplay() {
        String characterSetSmall = "nomufv!weilhkj@,tscdzg.'yabrpxq?";
        String characterSetCaps = "NOMUFV!WEILHKJ@_TSCDZG-\"YABRPXQ*";

        if(eightVimInputMethodService.areCharactersCapitalized()){
            return characterSetCaps;
        }

        return characterSetSmall;
    }

    private List<PointF> getCharacterDisplayPointsOnTheLineSegment(LineSegment lineSegment, int numberOfCharactersToDisplay) {
        List<PointF> pointsOfCharacterDisplay = new ArrayList<>();

        //Assuming we got to derive 4 points
        double spacingBetweenPoints = lineSegment.getLength()/numberOfCharactersToDisplay;
        float xOffset = getXOffset(lineSegment);
        float yOffset = getYOffset(lineSegment);

        for(int i = 0 ; i < 4 ; i++){
            PointF nextPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(lineSegment.getStartingPoint(), lineSegment.getDirectionOfLineInDegree(), (spacingBetweenPoints * i));
            PointF displayPointInAntiClockwiseDirection = new PointF(nextPoint.x + xOffset, nextPoint.y + yOffset);
            PointF displayPointInClockwiseDirection = new PointF(nextPoint.x + (xOffset*-1), nextPoint.y + (yOffset*-1));
            pointsOfCharacterDisplay.add(displayPointInAntiClockwiseDirection);
            pointsOfCharacterDisplay.add(displayPointInClockwiseDirection);
        }
        return pointsOfCharacterDisplay;
    }

    private float getYOffset(LineSegment lineSegment) {
        int ySign = (lineSegment.getStartingPoint().y - lineSegment.getEndPoint().y)>0?-1:1;
        int slopeSign = lineSegment.isSlopePositive()?1:-1;
        int aggregateSign = ySign*slopeSign*-1;
        float offset = 50;
        return offset*aggregateSign;
    }

    private float getXOffset(LineSegment lineSegment) {
        int xSign = (lineSegment.getStartingPoint().x - lineSegment.getEndPoint().x)>0?-1:1;
        int slopeSign = lineSegment.isSlopePositive()?1:-1;
        int aggregateSign = xSign*slopeSign;
        float offset = 50;
        return offset*aggregateSign;
    }

    public FingerPosition getCurrentFingerPosition(PointF position) {
        if(circle.isPointInsideCircle(position)){
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return circle.getSectorOfPoint(position);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        PointF position = new PointF((int)e.getX(), (int)e.getY());
        FingerPosition currentFingerPosition = getCurrentFingerPosition(position);
        switch(e.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mainKeyboardActionListener.movementStarted(currentFingerPosition);
                break;

            case MotionEvent.ACTION_MOVE:
                mainKeyboardActionListener.movementContinues(currentFingerPosition);
                break;

            case MotionEvent.ACTION_UP:
                mainKeyboardActionListener.movementEnds();
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

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
            height = Math.round(0.8f * width);
        }

        // Calculate the diameter with the circle width to image width ratio 260:800,
        // and divide in half to get the radius
        float radius = (0.325f * width) / 2;
        PointF centre = new PointF((width/2),(height/2));
        circle = new Circle(centre, radius);
        // Set the new size
        setMeasuredDimension(width, height);
    }
}
