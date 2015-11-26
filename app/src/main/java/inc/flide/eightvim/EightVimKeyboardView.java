package inc.flide.eightvim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.geometry.Circle;
import inc.flide.eightvim.geometry.GeometricUtilities;
import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.logging.Logger;

public class EightVimKeyboardView extends View{

    private EightVimInputMethodService eightVimInputMethodService;

    private List<FingerPosition> movementSequence;
    private FingerPosition currentFingerPosition;
    private Circle circle;
    private boolean isShiftPressed;

    Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

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
        initializeKeyboardActionMap();
        setHapticFeedbackEnabled(true);
        movementSequence = new ArrayList<>();
        currentFingerPosition = FingerPosition.NO_TOUCH;
    }

    private void initializeKeyboardActionMap() {

        InputStream inputStream = null;
        try{
            inputStream = getResources().openRawResource(getResources().getIdentifier("raw/keyboard_actions", "raw", eightVimInputMethodService.getPackageName()));
            KeyboardActionXmlParser keyboardActionXmlParser = new KeyboardActionXmlParser(inputStream);
            keyboardActionMap = keyboardActionXmlParser.readKeyboardActionMap();

        } catch (XmlPullParserException exception){
            exception.printStackTrace();
        } catch (IOException exception){
            exception.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }
        finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

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

        //create the circle.getCentre() circle
        RectF oval = new RectF();
        oval.set(circle.getCentre().x-circle.getRadius(), circle.getCentre().y-circle.getRadius(), circle.getCentre().x+circle.getRadius(), circle.getCentre().y+circle.getRadius());
        canvas.drawArc(oval, 0f, 360f, false, paint);
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(),paint);

        float r = circle.getRadius() + 200;
        canvas.drawLine(circle.getCentre().x, circle.getCentre().y, circle.getCentre().x-r, circle.getCentre().y-r, paint);
        canvas.drawLine(circle.getCentre().x, circle.getCentre().y, circle.getCentre().x-r, circle.getCentre().y+r, paint);
        canvas.drawLine(circle.getCentre().x, circle.getCentre().y, circle.getCentre().x + r, circle.getCentre().y + r, paint);
        canvas.drawLine(circle.getCentre().x, circle.getCentre().y, circle.getCentre().x + r, circle.getCentre().y - r, paint);
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
        float radius = (0.325f * width) / 2;
        PointF centre = new PointF((width/2),(height/2));
        circle = new Circle(centre, radius);
        // Set the new size
        setMeasuredDimension(width, height);
        Logger.v(this, "onMeasure returns");
    }



    /** Get the number of the sector that point p is in
     *  @return 0: right, 1: top, 2: left, 3: bottom */
    private FingerPosition getSector(PointF p)
    {
        double angleDouble = GeometricUtilities.getAngleOfPointWithRespectToCentreOfCircle(p, circle);
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

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
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

    private void movementStarted(MotionEvent e)
    {
        PointF position = new PointF(e.getX(), e.getY());
        currentFingerPosition = getCurrentFingerPosition(position);
        movementSequence.clear();
        movementSequence.add(currentFingerPosition);
    }

    private void movementContinues(MotionEvent e)
    {
        PointF position = new PointF(e.getX(), e.getY());
        FingerPosition lastKnownFingerPosition = currentFingerPosition;
        currentFingerPosition = getCurrentFingerPosition(position);

        boolean isFingerPositionChanged = (lastKnownFingerPosition != currentFingerPosition);

        if(isFingerPositionChanged){
            movementSequence.add(currentFingerPosition);
            if(currentFingerPosition == FingerPosition.INSIDE_CIRCLE){
                processMovementSequence();
                movementSequence.add(currentFingerPosition);
            }
        }
    }

    private void movementEnds(MotionEvent e)
    {
        currentFingerPosition = FingerPosition.NO_TOUCH;
        movementSequence.add(currentFingerPosition);
        processMovementSequence();
    }


    private void processMovementSequence() {

        KeyboardAction keyboardAction = keyboardActionMap.get(movementSequence);
        boolean isMovementValid = true;
        if(keyboardAction == null){
            Logger.Warn(this, "No Action Mapping has been defined for the given Sequence : " + movementSequence.toString());
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()){
            case INPUT_TEXT:
                handleInputText(keyboardAction);
                break;
            case INPUT_KEY:
                handleInputKey(keyboardAction);
                break;
            case INPUT_SPECIAL:
                handleSpecialInput(keyboardAction);
                break;
            default:
                Logger.Warn(this, "Action Type Undefined : " + keyboardAction.getKeyboardActionType().toString());
                isMovementValid = false;
                break;
        }
        if(isMovementValid){
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        //Clear the queue before this function finishes off
        movementSequence.clear();
    }

    private void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1 && isShiftPressed){
            eightVimInputMethodService.sendText(keyboardAction.getText().toUpperCase());
            isShiftPressed = false;
        }else{
            eightVimInputMethodService.sendText(keyboardAction.getText());
        }
    }

    private void handleInputKey(KeyboardAction keyboardAction) {
        eightVimInputMethodService.sendKey(keyboardAction.getKeyEventCode());
    }

    private void handleSpecialInput(KeyboardAction keyboardAction) {
        switch (keyboardAction.getKeyEventCode()){
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                isShiftPressed = true;
                break;
            default:
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getKeyEventCode());
                break;
        }
    }

}
