package inc.flide.vim8.views.mainKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Circle;
import inc.flide.vim8.geometry.GeometricUtilities;
import inc.flide.vim8.geometry.LineSegment;
import inc.flide.vim8.keyboardActionListners.MainKeyboardActionListener;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.utilities.Utilities;

public class XboardView extends View{

    private MainKeyboardActionListener actionListener;

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
        actionListener = new MainKeyboardActionListener((MainInputMethodService) context, this);
        setHapticFeedbackEnabled(true);
    }

    private final int offset = 15;
    private final int lengthOfLineDemarcatingSectors = 250;

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
        for(int i =0; i<4 ; i++) {
            int angle = 45+(i*90);
            PointF startingPoint = circle.getPointOnCircumferenceAtDegreeAngle(angle);
            LineSegment lineSegment = new LineSegment(startingPoint, angle, lengthOfLineDemarcatingSectors);
            sectorDemarcatingLines.add(lineSegment);
            canvas.drawLine(lineSegment.getStartingPoint().x, lineSegment.getStartingPoint().y, lineSegment.getEndPoint().x, lineSegment.getEndPoint().y, paint);
        }

        //the text along the lines
        paint.setTextSize(Constants.TEXT_SIZE);
        //paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.black));
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Regular.otf");
        paint.setTypeface(font);


        float characterHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        String charactersToDisplay = getCharacterSetToDisplay();
        List<PointF> listOfPointsOfDisplay = new ArrayList<>();
        for(int i=0; i<4; i++) {
            LineSegment currentLine = sectorDemarcatingLines.get(i);
            listOfPointsOfDisplay.addAll(getCharacterDisplayPointsOnTheLineSegment(currentLine, 4, characterHeight));
        }
        float[] pointsOfDisplay = Utilities.convertPointFListToPrimitiveFloatArray(listOfPointsOfDisplay);
        canvas.drawPosText(charactersToDisplay, pointsOfDisplay, paint);

    }

    private String getCharacterSetToDisplay() {
        String characterSetSmall = "nomufv!weilhkj@,tscdzg.'yabrpxq?";
        String characterSetCaps = "NOMUFV!WEILHKJ@_TSCDZG-\"YABRPXQ*";

        if(actionListener.areCharactersCapitalized()){
            return characterSetCaps;
        }

        return characterSetSmall;
    }

    private List<PointF> getCharacterDisplayPointsOnTheLineSegment(LineSegment lineSegment, int numberOfCharactersToDisplay, float height) {
        List<PointF> pointsOfCharacterDisplay = new ArrayList<>();

        //Assuming we got to derive 4 points
        double spacingBetweenPoints = lineSegment.getLength()/numberOfCharactersToDisplay;

        for(int i = 0 ; i < 4 ; i++){
            PointF nextPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(lineSegment.getStartingPoint(), lineSegment.getDirectionOfLineInDegree(), (spacingBetweenPoints * i));
            PointF displayPointInAntiClockwiseDirection = new PointF(nextPoint.x + computeAntiClockwiseXOffset(lineSegment, height)
                    , nextPoint.y + computeAntiClockwiseYOffset(lineSegment, height));

            PointF displayPointInClockwiseDirection     = new PointF(nextPoint.x + computeClockwiseXOffset(lineSegment, height)
                    , nextPoint.y+ computeClockwiseYOffset(lineSegment, height));

            pointsOfCharacterDisplay.add(displayPointInAntiClockwiseDirection);
            pointsOfCharacterDisplay.add(displayPointInClockwiseDirection);
        }
        return pointsOfCharacterDisplay;
    }

    private float computeClockwiseYOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if(lineSegment.isSlopePositive()){
            return offset + (isXDirectionPositive?height:-height);
        }
        return 0;
    }

    private float computeAntiClockwiseYOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);
        if(lineSegment.isSlopePositive()){
            return offset;
        }
        return isXDirectionPositive?-height:height;
    }

    private float computeClockwiseXOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if (lineSegment.isSlopePositive()) {
            return 0;
        }
        return isXDirectionPositive?height:-height;
    }

    private float computeAntiClockwiseXOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if (lineSegment.isSlopePositive()){
            return isXDirectionPositive?height:-height;
        }
        return 0;
    }

    private FingerPosition getCurrentFingerPosition(PointF position) {
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
                actionListener.movementStarted(currentFingerPosition);
                break;

            case MotionEvent.ACTION_MOVE:
                actionListener.movementContinues(currentFingerPosition);
                break;

            case MotionEvent.ACTION_UP:
                actionListener.movementEnds();
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        if(getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
        {
            //Landscape is just un-usable right now.
            // TODO: Landscape mode requires more clarity, what exactly do you want to do?
            width = Math.round(1.33f * height);

        }
        else  // Portrait mode
        {
            height = Math.round(0.8f * width);

        }

        float radius = (0.325f * width) / 2;
        PointF centre = new PointF((width / 2), (height/ 2));

        //initial value of centre.x is 450 n centre.y is 360

        int offset_x = 0  ,offset_y = 0 ;

        //shared prefrence for x-axis

        SharedPreferences sp = this.getContext().getSharedPreferences(this.getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        offset_x = sp.getInt(this.getContext().getString(R.string.x_postion_values),0);

        offset_x = offset_x * 26;

        //shared prefrence for y-axis

        SharedPreferences sp_offset_y = this.getContext().getSharedPreferences(this.getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        offset_y = sp_offset_y.getInt(this.getContext().getString(R.string.y_postion_values),0);

        offset_y = offset_y * 26;

        centre.x = centre.x + offset_x;
        centre.y = centre.y + offset_y;

        circle = new Circle(centre, radius);
        setMeasuredDimension(width, height);
    }
}
