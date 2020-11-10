package inc.flide.vim8.views.mainKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Circle;
import inc.flide.vim8.geometry.GeometricUtilities;
import inc.flide.vim8.geometry.LineSegment;
import inc.flide.vim8.keyboardActionListners.MainKeyboardActionListener;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.utilities.Utilities;



public class XboardView extends View {

    private MainKeyboardActionListener actionListener;

    private Circle circle;
    List<LineSegment> sectorDemarcatingLines = new ArrayList<>();
    List<PointF> listOfPointsOfDisplay = new ArrayList<>();

    Path path = new Path();
    Context context;

    GestureDetector gestureDetector;
    Paint backgroundPaint = new Paint();
    Paint foregroundPaint = new Paint();

    public XboardView(Context context) {
        super(context);
        initialize(context);
    }

    public XboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
        gestureDetector = new GestureDetector(context,new GestureListener());
        this.context = context;
    }


    public XboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        actionListener = new MainKeyboardActionListener((MainInputMethodService) context, this);
        setHapticFeedbackEnabled(true);

        backgroundPaint.setARGB(255, 255, 255, 255);

        foregroundPaint.setARGB(255, 0, 0, 0);
        foregroundPaint.setAntiAlias(true);
        foregroundPaint.setStrokeJoin(Paint.Join.ROUND);
        foregroundPaint.setTextSize(Constants.TEXT_SIZE);
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Regular.otf");
        foregroundPaint.setTypeface(font);
    }

    private final int offset = 15;

    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawColor(backgroundPaint.getColor());

        SharedPreferences sp = this.getContext().getSharedPreferences(this.getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        if(sp.getBoolean(this.getContext().getString(R.string.user_preferred_typing_trail_visibility),true))
        {
            paintTypingTrail(canvas);

        }

        foregroundPaint.setStrokeWidth(5);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        //The centre circle
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(), foregroundPaint);
        //The lines demarcating the sectors
        for (LineSegment lineSegment: sectorDemarcatingLines) {
            canvas.drawLine(lineSegment.getStartingPoint().x, lineSegment.getStartingPoint().y, lineSegment.getEndPoint().x, lineSegment.getEndPoint().y, foregroundPaint);
        }

        //the text along the lines
        foregroundPaint.setStrokeWidth(2);
        foregroundPaint.setStyle(Paint.Style.FILL);

        String charactersToDisplay = getCharacterSetToDisplay();
        float[] pointsOfDisplay = Utilities.convertPointFListToPrimitiveFloatArray(listOfPointsOfDisplay);
        canvas.drawPosText(charactersToDisplay, pointsOfDisplay, foregroundPaint);

    }

    private void paintTypingTrail(Canvas canvas) {
        float[] pathPos =new float[2];
        Paint typingTrailPaint = new Paint();

        if (path != null) {
            final short steps = 150;
            final byte stepDistance = 5;
            final byte maxTrailRadius = 14;
            PathMeasure pathMeasure = new PathMeasure();
            pathMeasure.setPath(path, false);
            Random random = new Random();
            final float pathLength = pathMeasure.getLength();
            for (short i = 1; i <= steps; i++) {
                final float distance = pathLength - i * stepDistance;
                if (distance >= 0) {
                    final float trailRadius = maxTrailRadius * (1 - (float) i / steps);
                    pathMeasure.getPosTan(distance, pathPos, null);
                    final float x = pathPos[0] + random.nextFloat() - trailRadius;
                    final float y = pathPos[1] + random.nextFloat() - trailRadius;

                    SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(this.getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                    String color_choosed = sharedPreferences.getString(this.getContext().getString(R.string.color_selection),"Red");

                    if(color_choosed.equals("Red"))
                    {
                        typingTrailPaint.setShader(new RadialGradient(
                                x,
                                y,
                                trailRadius > 0 ? trailRadius : Float.MIN_VALUE,
                                ColorUtils.setAlphaComponent(Color.RED, random.nextInt(0xff)),
                                Color.TRANSPARENT,
                                Shader.TileMode.CLAMP
                        ));
                    }
                    else if(color_choosed.equals("Green"))
                    {
                        typingTrailPaint.setShader(new RadialGradient(
                                x,
                                y,
                                trailRadius > 0 ? trailRadius : Float.MIN_VALUE,
                                ColorUtils.setAlphaComponent(Color.GREEN, random.nextInt(0xff)),
                                Color.TRANSPARENT,
                                Shader.TileMode.CLAMP
                        ));
                    }
                    else if(color_choosed.equals("Yellow"))
                    {
                        typingTrailPaint.setShader(new RadialGradient(
                                x,
                                y,
                                trailRadius > 0 ? trailRadius : Float.MIN_VALUE,
                                ColorUtils.setAlphaComponent(Color.YELLOW, random.nextInt(0xff)),
                                Color.TRANSPARENT,
                                Shader.TileMode.CLAMP
                        ));
                    }
                    else if(color_choosed.equals("Blue")){
                        typingTrailPaint.setShader(new RadialGradient(
                                x,
                                y,
                                trailRadius > 0 ? trailRadius : Float.MIN_VALUE,
                                ColorUtils.setAlphaComponent(Color.BLUE, random.nextInt(0xff)),
                                Color.TRANSPARENT,
                                Shader.TileMode.CLAMP
                        ));
                    }

                    canvas.drawCircle(x, y, trailRadius, typingTrailPaint);
                }
            }
        }
        canvas.drawPath(path,typingTrailPaint);
    }


    private String getCharacterSetToDisplay() {
        String characterSetSmall = "nomufv!weilhkj@,tscdzg.'yabrpxq?";
        String characterSetCaps = "NOMUFV!WEILHKJ@_TSCDZG-\"YABRPXQ*";

        if (actionListener.areCharactersCapitalized()) {
            return characterSetCaps;
        }

        return characterSetSmall;
    }

    private List<PointF> getCharacterDisplayPointsOnTheLineSegment(LineSegment lineSegment, int numberOfCharactersToDisplay, float height) {
        List<PointF> pointsOfCharacterDisplay = new ArrayList<>();

        //Assuming we got to derive 4 points
        double spacingBetweenPoints = lineSegment.getLength() / numberOfCharactersToDisplay;

        for (int i = 0; i < 4; i++) {
            PointF nextPoint = GeometricUtilities.findPointSpecifiedDistanceAwayInGivenDirection(lineSegment.getStartingPoint(), lineSegment.getDirectionOfLineInDegree(), (spacingBetweenPoints * i));
            PointF displayPointInAntiClockwiseDirection = new PointF(nextPoint.x + computeAntiClockwiseXOffset(lineSegment, height)
                    , nextPoint.y + computeAntiClockwiseYOffset(lineSegment, height));

            PointF displayPointInClockwiseDirection = new PointF(nextPoint.x + computeClockwiseXOffset(lineSegment, height)
                    , nextPoint.y + computeClockwiseYOffset(lineSegment, height));

            pointsOfCharacterDisplay.add(displayPointInAntiClockwiseDirection);
            pointsOfCharacterDisplay.add(displayPointInClockwiseDirection);
        }
        return pointsOfCharacterDisplay;
    }

    private float computeClockwiseYOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if (lineSegment.isSlopePositive()) {
            return offset + (isXDirectionPositive ? height : -height);
        }


        return 0;
    }

    private float computeAntiClockwiseYOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);
        if (lineSegment.isSlopePositive()) {
            return offset;
        }
        return isXDirectionPositive ? -height : height;
    }

    private float computeClockwiseXOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if (lineSegment.isSlopePositive()) {
            return 0;
        }
        return isXDirectionPositive ? height : -height;
    }

    private float computeAntiClockwiseXOffset(LineSegment lineSegment, float height) {
        double angle = lineSegment.getDirectionOfLineInDegree();
        boolean isXDirectionPositive = (angle > 0 && angle < 90) || (angle > 270 && angle < 360);

        if (lineSegment.isSlopePositive()) {
            return isXDirectionPositive ? height : -height;
        }
        return 0;
    }

    private FingerPosition getCurrentFingerPosition(PointF position) {
        if (circle.isPointInsideCircle(position)) {
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return circle.getSectorOfPoint(position);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        // event when double tap occurs


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            path.addCircle(x, y, 50, Path.Direction.CW);

            // clean drawing area on double tap
            path.reset();

            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            return true;
        }

    }

        @SuppressLint("ClickableViewAccessibility")
        @Override
       public boolean onTouchEvent(MotionEvent e) {
        PointF position = new PointF((int) e.getX(), (int) e.getY());
        FingerPosition currentFingerPosition = getCurrentFingerPosition(position);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                 actionListener.movementStarted(currentFingerPosition);
                 path.moveTo(e.getX(),e.getY());
                return true;

            case MotionEvent.ACTION_MOVE:
                actionListener.movementContinues(currentFingerPosition);
                path.lineTo(e.getX(), e.getY());
                break;

            case MotionEvent.ACTION_UP:
                actionListener.movementEnds();
            case MotionEvent.ACTION_POINTER_DOWN:
                 path.reset();
                break;
            default:
                return false;
        }
        gestureDetector.onTouchEvent(e);

        // Schedules a repaint.
        invalidate();
        return true;
    }


    @SuppressLint("DrawAllocation")
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            //Landscape is just un-usable right now.
            // TODO: Landscape mode requires more clarity, what exactly do you want to do?
            width = Math.round(1.33f * height);
        } else  // Portrait mode
        {
            height = Math.round(0.8f * width);
        }

        SharedPreferences sp = this.getContext().getSharedPreferences(this.getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        float spRadiusValue = sp.getFloat(this.getContext().getString(R.string.x_board_circle_radius_size_factor_key), 0.3f);
        float radius = (spRadiusValue * width) / 2;


        @SuppressLint("DrawAllocation")
        PointF centre = new PointF((width / 2), (height / 2));
        centre.x = centre.x + ((sp.getInt(this.getContext().getString(R.string.x_board_circle_centre_x_offset_key), 0)) * 26);
        centre.y = centre.y + ((sp.getInt(this.getContext().getString(R.string.x_board_circle_centre_y_offset_key), 0)) * 26);

        circle = new Circle(centre, radius);
        float characterHeight = foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent;
        for (int i = 0; i < 4; i++) {
            int angle = 45 + (i * 90);
            PointF startingPoint = circle.getPointOnCircumferenceAtDegreeAngle(angle);
            int lengthOfLineDemarcatingSectors = 250;
            @SuppressLint("DrawAllocation")
            LineSegment lineSegment = new LineSegment(startingPoint, angle, lengthOfLineDemarcatingSectors);
            sectorDemarcatingLines.add(lineSegment);
            listOfPointsOfDisplay.addAll(getCharacterDisplayPointsOnTheLineSegment(lineSegment, 4, characterHeight));
        }

        setMeasuredDimension(width, height);
    }

}
