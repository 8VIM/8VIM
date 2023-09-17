package inc.flide.vim8.views.mainkeyboard;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;
import static inc.flide.vim8.models.QuadrantKt.NUMBER_OF_SECTORS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Circle;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.ime.KeyboardTheme;
import inc.flide.vim8.ime.actionlisteners.MainKeypadActionListener;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.FingerPosition;
import inc.flide.vim8.models.LayerLevel;

public class XpadView extends View {
    public static final int XPAD_ICON_ALPHA = 85;
    public static final int XPAD_CIRCLE_OFFSET_FACTOR = 26;
    public static final float XPAD_CIRCLE_RADIUS_FACTOR = 40f;
    public static final float XPAD_LETTER_HIGHLIGHT_ROUNDNESS = 25f;
    public static final float FOREGROUND_STROKE_FACTOR = 0.75f;
    public static final float LETTER_BACKGROUND_BLEND_RATIO = 0.5f;
    public static final int DEGREE_45 = 45;
    public static final int DEGREE_90 = 90;
    public static final int UPPER_LETTER_Y_IDX_OFFSET = 1;
    public static final int LOWER_LETTER_X_IDX_OFFSET = 2;
    public static final int LOWER_LETTER_Y_IDX_OFFSET = 3;
    private static final short TRAIL_STEPS = 150;
    private static final byte TRAIL_STEP_DISTANCE = 5;
    private static final byte TRAIL_MAX_RADIUS = 14;
    private final Path typingTrailPath = new Path();
    private final Paint backgroundPaint = new Paint();
    private final Paint foregroundPaint = new Paint();
    private final Paint foregroundHighlightPaint = new Paint();
    private final Paint typingTrailPaint = new Paint();
    private final Paint letterBackgroundPaint = new Paint();
    private final Paint letterBackgroundOutlinePaint = new Paint();
    private final PointF circleCenter = new PointF();
    private final Circle circle = new Circle();
    private final Dimension keypadDimension = new Dimension();
    private final Matrix xformMatrix = new Matrix();
    // There are 4 sectors, each has 4 letters above, and 4 below.
    // Finally, each letter position has an x & y co-ordinate.
    private final float[] letterPositions =
            new float[NUMBER_OF_SECTORS * 2 * NUMBER_OF_SECTORS * 2];
    private final Path sectorLines = new Path();
    private final RectF sectorLineBounds = new RectF();
    private final float[] trialPathPos = new float[2];
    private final PathMeasure pathMeasure = new PathMeasure();
    private MainKeypadActionListener actionListener;
    private AppPrefs prefs;
    private KeyboardTheme keyboardTheme;

    public XpadView(Context context) {
        super(context);
        initialize(context);
    }

    public XpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public XpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        prefs = appPreferenceModel().java();
        keyboardTheme = KeyboardTheme.getInstance();
        keyboardTheme.onChange(this::updateColors);

        prefs.getLayout().getCurrent().observe(newValue -> invalidate());
        AppPrefs.Keyboard.Circle circlePrefs = prefs.getKeyboard().getCircle();
        circlePrefs.getRadiusSizeFactor().observe(this::onCirclePrefsChanged);
        circlePrefs.getXCentreOffset().observe(this::onCirclePrefsChanged);
        circlePrefs.getYCentreOffset().observe(this::onCirclePrefsChanged);

        AppPrefs.Keyboard.Display displayPrefs = prefs.getKeyboard().getDisplay();
        displayPrefs.getShowSectorIcons().observe(this::onDisplayPrefsChanged);
        displayPrefs.getShowLettersOnWheel().observe(this::onDisplayPrefsChanged);

        Typeface font = Typeface.createFromAsset(context.getAssets(), "SF-UI-Display-Regular.otf");
        Typeface fontBold = Typeface.createFromAsset(context.getAssets(), "SF-UI-Display-Bold.otf");

        updateColors();
        setForegroundPaint(foregroundPaint, font);
        setForegroundPaint(foregroundHighlightPaint, fontBold);

        actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
        setHapticFeedbackEnabled(true);
    }

    private void onCirclePrefsChanged(int newValue) {
        this.computeComponentPositions(this.getWidth(), this.getHeight());
        this.invalidate();
    }

    private void onDisplayPrefsChanged(boolean newValue) {
        this.invalidate();
    }

    private void updateColors() {
        backgroundPaint.setColor(keyboardTheme.getBackgroundColor());
        foregroundPaint.setColor(keyboardTheme.getForegroundColor());
        invalidate();
    }

    private void computeComponentPositions(int fullWidth, int fullHeight) {
        AppPrefs.Keyboard.Circle circlePrefs = prefs.getKeyboard().getCircle();
        float spRadiusValue = circlePrefs.getRadiusSizeFactor().get();
        boolean preferredSidebarLeft = prefs.getKeyboard().getSidebar().isOnLeft().get();

        float radius = (spRadiusValue / XPAD_CIRCLE_RADIUS_FACTOR * keypadDimension.height) / 2;

        int offsetX =
                (circlePrefs.getXCentreOffset().get()) * XPAD_CIRCLE_OFFSET_FACTOR;
        int offsetY =
                (circlePrefs.getYCentreOffset().get()) * XPAD_CIRCLE_OFFSET_FACTOR;

        float characterHeight =
                foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent;
        // We chop off a bit of the right side of the view width from the keypadDimension (see onMeasure),
        // this introduces a bit of asymmetry which we have to compensate for here.
        int keypadXOffset = fullWidth - keypadDimension.width;
        // If the xOffset is to the right, we can spread into the extra padding space.
        int smallDim = Math.min(offsetX > 0 ? fullWidth / 2 - offsetX + keypadXOffset
                        // If xOffset goes to the left, restrict to keypadDimension.
                        : keypadDimension.width / 2 + offsetX,
                fullHeight / 2 - Math.abs(offsetY));
        // Compute the length of sector lines, such that they stop a little before hitting the edge of the view.
        float lengthOfLineDemarcatingSectors = (float) Math.hypot(smallDim, smallDim)
                - radius - characterHeight;
        if (isTabletInLandscape()) {
            circleCenter.x = lengthOfLineDemarcatingSectors + offsetX;
            if (!preferredSidebarLeft) {
                circleCenter.x = keypadDimension.width - circleCenter.x;
            }
        } else {
            circleCenter.x = (keypadDimension.width / 2f) + offsetX;
        }
        circleCenter.y = (keypadDimension.height / 2f) + offsetY;

        circle.centre = circleCenter;
        circle.radius = radius;


        // Compute sector demarcation lines as if they were all going orthogonal (like a "+").
        // This is easier to compute.  Later we apply rotation to orient the lines properly (like an "x").
        sectorLines.rewind();
        sectorLines.moveTo(circleCenter.x + radius, circleCenter.y);
        sectorLines.rLineTo(lengthOfLineDemarcatingSectors, 0);
        sectorLines.moveTo(circleCenter.x - radius, circleCenter.y);
        sectorLines.rLineTo(-lengthOfLineDemarcatingSectors, 0);
        sectorLines.moveTo(circleCenter.x, circleCenter.y + radius);
        sectorLines.rLineTo(0, lengthOfLineDemarcatingSectors);
        sectorLines.moveTo(circleCenter.x, circleCenter.y - radius);
        sectorLines.rLineTo(0, -lengthOfLineDemarcatingSectors);

        // Compute the first set of points going straight to the "east" (aka, rightwards).
        // Then apply repeated rotation (45, then 90 x4) to get the final positions.
        computeLettersPositions(characterHeight, lengthOfLineDemarcatingSectors);

        xformMatrix.reset();
        xformMatrix.postRotate(DEGREE_45, circleCenter.x, circleCenter.y);
        xformMatrix.mapPoints(letterPositions, 0, letterPositions, 0, 8);
        sectorLines.transform(xformMatrix);

        xformMatrix.reset();
        xformMatrix.postRotate(DEGREE_90, circleCenter.x, circleCenter.y);
        for (int i = 1; i < NUMBER_OF_SECTORS; i++) {
            xformMatrix.mapPoints(letterPositions,
                    NUMBER_OF_SECTORS * NUMBER_OF_SECTORS * i,
                    letterPositions,
                    NUMBER_OF_SECTORS * NUMBER_OF_SECTORS * (i - 1),
                    8);
        }

        // Canvas.drawPosText() draws from the bottom,
        // so we need to offset downwards a bit to compensate.
        xformMatrix.reset();
        xformMatrix.postTranslate(0, 3 * characterHeight / 16);
        xformMatrix.mapPoints(letterPositions);

        sectorLines.computeBounds(sectorLineBounds, false); // Used to position icons
    }

    private boolean isTabletInLandscape() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && configuration.screenHeightDp >= 480;
    }

    private void computeLettersPositions(float characterHeight,
                                         float lengthOfLineDemarcatingSectors) {
        float eastEdge = circleCenter.x + circle.radius + characterHeight / 2;
        for (int i = 0; i < NUMBER_OF_SECTORS; i++) {
            float dx = i * lengthOfLineDemarcatingSectors / ((float) NUMBER_OF_SECTORS);
            letterPositions[NUMBER_OF_SECTORS * i] = eastEdge + dx;
            letterPositions[NUMBER_OF_SECTORS * i + UPPER_LETTER_Y_IDX_OFFSET] =
                    circleCenter.y - characterHeight / 2; // upper letter
            letterPositions[NUMBER_OF_SECTORS * i + LOWER_LETTER_X_IDX_OFFSET] = eastEdge + dx;
            letterPositions[NUMBER_OF_SECTORS * i + LOWER_LETTER_Y_IDX_OFFSET] =
                    circleCenter.y + characterHeight / 2; // lower letter
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        keypadDimension.width = parentWidth;
        keypadDimension.height = parentHeight;

        setMeasuredDimension(keypadDimension.width, keypadDimension.height);
        // this.getWidth() returns 0 at this point, parentWidth (& height) give the correct result.
        computeComponentPositions(parentWidth, parentHeight);
    }


    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundPaint.getColor());

        boolean userPrefersTypingTrail = prefs.getKeyboard().getTrail().isVisible().get();
        if (userPrefersTypingTrail) {
            paintTypingTrail(canvas);
        }

        float density = getResources().getDisplayMetrics().density;
        foregroundPaint.setStrokeWidth(2 * density);
        foregroundPaint.setStyle(Paint.Style.STROKE);

        //The centre circle
        canvas.drawCircle(circle.centre.x, circle.centre.y, circle.radius,
                foregroundPaint);
        canvas.drawPath(sectorLines, foregroundPaint); //The lines demarcating the sectors

        // Converting float value to int
        int centreXValue = (int) circle.centre.x;
        int centreYValue = (int) circle.centre.y;

        boolean userPrefersSectorIcons = prefs.getKeyboard().getDisplay().getShowSectorIcons().get();

        if (userPrefersSectorIcons) {
            setupSectorIcons(centreXValue, centreYValue, canvas);
        }

        //the text along the lines
        boolean userPreferWheelLetters = prefs.getKeyboard().getDisplay().getShowLettersOnWheel().get();

        if (userPreferWheelLetters) {
            // Make the color the same as the typing trail, but blend it with white
            // because it's too hard to see a black font on dark backgrounds.
            int letterBackgroundColor =
                    ColorUtils.blendARGB(typingTrailPaint.getColor(), Color.WHITE,
                            LETTER_BACKGROUND_BLEND_RATIO);

            letterBackgroundPaint.setColor(letterBackgroundColor);

            letterBackgroundOutlinePaint.setColor(Color.BLACK);
            letterBackgroundOutlinePaint.setStyle(Paint.Style.STROKE);
            letterBackgroundOutlinePaint.setStrokeWidth(3f);

            letterBackgroundPaint.setAntiAlias(true);
            letterBackgroundOutlinePaint.setAntiAlias(true);

            // Paint for the regular and bold fonts.
            setForeground(foregroundPaint, density);
            setForeground(foregroundHighlightPaint, density);

            String characterSet = getCharacterSetToDisplay();
            for (int i = 0; i < characterSet.length(); i++) {
                Paint paint = foregroundPaint;
                String letter = String.valueOf(characterSet.charAt(i));
                if (actionListener.getCurrentLetter() != null
                        && String.valueOf(actionListener.getCurrentLetter().charAt(0)).equals(letter)) {
                    paint = foregroundHighlightPaint;

                    // Draw a box around the current letter.
                    float characterHeightWidth =
                            foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent;
                    canvas.drawRoundRect(
                            letterPositions[i * 2] - (characterHeightWidth / 2),
                            letterPositions[i * 2 + 1] - characterHeightWidth,
                            letterPositions[i * 2] + (characterHeightWidth / 2),
                            letterPositions[i * 2 + 1] + (characterHeightWidth / 2),
                            XPAD_LETTER_HIGHLIGHT_ROUNDNESS, XPAD_LETTER_HIGHLIGHT_ROUNDNESS,
                            letterBackgroundPaint
                    );

                    canvas.drawRoundRect(
                            letterPositions[i * 2] - (characterHeightWidth / 2),
                            letterPositions[i * 2 + 1] - characterHeightWidth,
                            letterPositions[i * 2] + (characterHeightWidth / 2),
                            letterPositions[i * 2 + 1] + (characterHeightWidth / 2),
                            XPAD_LETTER_HIGHLIGHT_ROUNDNESS, XPAD_LETTER_HIGHLIGHT_ROUNDNESS,
                            letterBackgroundOutlinePaint
                    );
                }
                canvas.drawText(letter, letterPositions[i * 2], letterPositions[i * 2 + 1], paint);
            }
        }
    }

    private void setForeground(Paint foregroundPaint, float density) {
        foregroundPaint.setStrokeWidth(FOREGROUND_STROKE_FACTOR * density);
        foregroundPaint.setStyle(Paint.Style.FILL);
        foregroundPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void setForegroundPaint(Paint paint, Typeface font) {
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        paint.setTypeface(font);
    }

    private void setupSectorIcons(int centreXValue, int centreYValue, Canvas canvas) {

        int iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
        int iconHalfWidth = iconSize / 2;
        int iconHalfHeight = iconSize / 2;
        sectorLines.computeBounds(sectorLineBounds, false);
        //Number pad icon (left side)
        int iconCenterX = (int) Math.max(sectorLineBounds.left, 0);
        int iconCenterY = centreYValue;
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.numericpad_vd_vector);

        //for Backspace icon (right side)
        iconCenterX = (int) Math.min(sectorLineBounds.right, canvas.getWidth());
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_backspace);

        //for Enter icon (bottom)
        iconCenterX = centreXValue;
        iconCenterY = (int) Math.min(sectorLineBounds.bottom, canvas.getHeight());
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_keyboard_return);

        //for caps lock and shift icon
        iconCenterY = (int) Math.max(sectorLineBounds.top, 0);
        int shiftIconToDisplay = R.drawable.ic_no_capslock;
        if (actionListener.isShiftSet()) {
            shiftIconToDisplay = R.drawable.ic_shift_engaged;
        }
        if (actionListener.isCapsLockSet()) {
            shiftIconToDisplay = R.drawable.ic_capslock_engaged;
        }
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                shiftIconToDisplay);
    }

    private void drawIconInSector(int coordinateX, int coordinateY, Canvas canvas, int resourceId) {
        int iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);

        VectorDrawableCompat iconVectorDrawable = VectorDrawableCompat
                .create(getContext().getResources(), resourceId, null);
        if (iconVectorDrawable == null) {
            return;
        }

        iconVectorDrawable.setBounds(coordinateX,
                coordinateY,
                coordinateX + iconSize,
                coordinateY + iconSize);
        iconVectorDrawable.setTint(keyboardTheme.getForegroundColor());
        iconVectorDrawable.setAlpha(XPAD_ICON_ALPHA);
        iconVectorDrawable.draw(canvas);
    }

    private void paintTypingTrail(Canvas canvas) {
        pathMeasure.setPath(typingTrailPath, false);
        final float pathLength = pathMeasure.getLength();

        for (short i = 1; i <= TRAIL_STEPS; i++) {
            final float distance = pathLength - i * TRAIL_STEP_DISTANCE;
            if (distance >= 0) {
                final float trailRadius = TRAIL_MAX_RADIUS * (1 - (float) i / TRAIL_STEPS);
                pathMeasure.getPosTan(distance, trialPathPos, null);
                final float x = trialPathPos[0];
                final float y = trialPathPos[1];
                canvas.drawCircle(x, y, trailRadius, typingTrailPaint);
            }
        }
    }

    private String getCharacterSetToDisplay() {
        LayerLevel layer = actionListener.findLayer();
        if (actionListener.areCharactersCapitalized()) {
            return actionListener.getUpperCaseCharacters(layer);
        }

        return actionListener.getLowerCaseCharacters(layer);
    }

    private FingerPosition getCurrentFingerPosition(PointF position) {
        if (circle.isPointInsideCircle(position)) {
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return circle.getSectorOfPoint(position);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        PointF position = new PointF((int) e.getX(), (int) e.getY());
        FingerPosition currentFingerPosition = getCurrentFingerPosition(position);
        invalidate();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                actionListener.movementStarted(currentFingerPosition);
                typingTrailPath.reset();
                typingTrailPath.moveTo(e.getX(), e.getY());
                typingTrailPaint.setColor(keyboardTheme.getTrailColor());
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                actionListener.movementContinues(currentFingerPosition);
                typingTrailPath.lineTo(e.getX(), e.getY());
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                typingTrailPath.reset();
                actionListener.movementEnds();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                typingTrailPath.reset();
                actionListener.movementCanceled();
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}