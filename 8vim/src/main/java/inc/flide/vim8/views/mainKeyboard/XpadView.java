package inc.flide.vim8.views.mainKeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Circle;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.FingerPosition;

public class XpadView extends View {
    private final Random rnd = new Random();
    private final Path typingTrailPath = new Path();
    private Paint backgroundPaint = new Paint();
    private Paint foregroundPaint = new Paint();
    private Paint foregroundBoldPaint = new Paint();
    private Paint typingTrailPaint = new Paint();
    private MainKeypadActionListener actionListener;
    private PointF circleCenter;
    private Circle circle;
    private final Dimension keypadDimension = new Dimension();

    private final Matrix xformMatrix = new Matrix();
    // There are 4 sectors, each has 4 letters above, and 4 below.
    // Finally, each letter position has an x & y co-ordinate.
    private final float[] letterPositions = new float[4 * 2 * 4 * 2];
    private final Path sectorLines = new Path();
    private final RectF sectorLineBounds = new RectF();

    private int backgroundColor;
    private int foregroundColor;
    private int trailColor;
    private final float[] trialPathPos = new float[2];
    private final PathMeasure pathMeasure = new PathMeasure();

    private boolean userPreferRandomTrailColor = false;

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

    private void updateColors(Context context) {
        Resources resources = getResources();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context);

        backgroundColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_board_bg_color_key),
                resources.getColor(R.color.defaultBoardBg));

        foregroundColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg));

        userPreferRandomTrailColor = sharedPreferenceHelper.getBoolean(
                resources.getString(R.string.pref_random_trail_color_key),
                false);

        trailColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_trail_color_key),
                resources.getColor(R.color.defaultTrail));

        backgroundPaint.setColor(backgroundColor);
        foregroundPaint.setColor(foregroundColor);
        typingTrailPaint.setColor(trailColor);
    }

    private void initialize(Context context) {
        SharedPreferenceHelper.getInstance(context).addListener(() -> {
            this.updateColors(context);
            this.computeComponentPositions(this.getWidth(), this.getHeight());
            this.invalidate();
        });
        updateColors(context);
        setForegroundPaint();

        actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
        setHapticFeedbackEnabled(true);


        circleCenter = new PointF();
        circle = new Circle();
    }

    private void computeComponentPositions(int fullWidth, int fullHeight) {
        Context context = getContext();
        SharedPreferenceHelper pref = SharedPreferenceHelper.getInstance(context);
        float spRadiusValue = pref.getInt(context.getString(R.string.pref_circle_scale_factor), 3);
        // TODO: Store constant in .xml file (but where?)
        float radius = (spRadiusValue / 40.f * keypadDimension.getWidth()) / 2;

        int xOffset = (pref.getInt(context.getString(R.string.pref_circle_x_offset_key), 0)) * 26;
        int yOffset = (pref.getInt(context.getString(R.string.pref_circle_y_offset_key), 0)) * 26;
        circleCenter.x = (keypadDimension.getWidth() / 2f) + xOffset;
        circleCenter.y = (keypadDimension.getHeight() / 2f) + yOffset;

        circle.setCentre(circleCenter);
        circle.setRadius(radius);

        float characterHeight = foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent;
        // We chop off a bit of the right side of the view width from the keypadDimension (see onMeasure),
        // this introduces a bit of asymmetry which we have to compensate for here.
        int keypadXOffset = fullWidth - keypadDimension.getWidth();
        // If the xOffset is to the right, we can spread into the extra padding space.
        int smallDim = Math.min(xOffset > 0 ? fullWidth / 2 - xOffset + keypadXOffset
                        // If xOffset goes to the left, restrict to keypadDimension.
                        : keypadDimension.getWidth() / 2 + xOffset,
                fullHeight / 2 - Math.abs(yOffset));
        // Compute the length of sector lines, such that they stop a little before hitting the edge of the view.
        float lengthOfLineDemarcatingSectors = (float) Math.hypot(smallDim, smallDim)
                - radius - characterHeight;

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
        float eastEdge = circleCenter.x + circle.getRadius() + characterHeight / 2;
        for (int i = 0; i < 4; i++) {
            float dx = i * lengthOfLineDemarcatingSectors / 4f;
            letterPositions[4 * i] = eastEdge + dx;
            letterPositions[4 * i + 1] = circleCenter.y - characterHeight / 2; // upper letter
            letterPositions[4 * i + 2] = eastEdge + dx;
            letterPositions[4 * i + 3] = circleCenter.y + characterHeight / 2; // lower letter
        }

        xformMatrix.reset();
        xformMatrix.postRotate(45, circleCenter.x, circleCenter.y);
        xformMatrix.mapPoints(letterPositions, 0, letterPositions, 0, 8);
        sectorLines.transform(xformMatrix);

        xformMatrix.reset();
        xformMatrix.postRotate(90, circleCenter.x, circleCenter.y);
        for (int i = 1; i < 4; i++) {
            xformMatrix.mapPoints(letterPositions, 4 * 4 * i, letterPositions, 4 * 4 * (i - 1), 8);
        }

        // Canvas.drawPosText() draws from the bottom,
        // so we need to offset downwards a bit to compensate.
        xformMatrix.reset();
        xformMatrix.postTranslate(0, 3 * characterHeight / 16);
        xformMatrix.mapPoints(letterPositions);

        sectorLines.computeBounds(sectorLineBounds, false); // Used to position icons
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        keypadDimension.setWidth((parentWidth / 6) * 5);
        keypadDimension.setHeight(parentHeight);

        setMeasuredDimension(keypadDimension.getWidth(), keypadDimension.getHeight());
        // this.getWidth() returns 0 at this point, parentWidth (& height) give the correct result.
        computeComponentPositions(parentWidth, parentHeight);
    }

    private Paint letterBackgroundPaint = new Paint();
    private Paint letterBackgroundOutlinePaint = new Paint();
    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawColor(backgroundPaint.getColor());

        boolean userPrefersTypingTrail = SharedPreferenceHelper
                .getInstance(getContext())
                .getBoolean(
                        this.getContext().getString(R.string.pref_typing_trail_visibility_key),
                        true);
        if (userPrefersTypingTrail) {
            paintTypingTrail(canvas);
        }

        float density = getResources().getDisplayMetrics().density;
        foregroundPaint.setStrokeWidth(2 * density);
        foregroundPaint.setStyle(Paint.Style.STROKE);

        //The centre circle
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(), foregroundPaint);
        canvas.drawPath(sectorLines, foregroundPaint); //The lines demarcating the sectors

        // Converting float value to int
        int centreXValue = (int) circle.getCentre().x;
        int centreYValue = (int) circle.getCentre().y;

        boolean userPrefersSectorIcons = SharedPreferenceHelper
                .getInstance(getContext())
                .getBoolean(
                        this.getContext().getString(R.string.pref_display_sector_icons_key),
                        true);
        if (userPrefersSectorIcons) {
            setupSectorIcons(centreXValue, centreYValue, canvas);
        }

        //the text along the lines
        boolean userPreferWheelLetters = SharedPreferenceHelper
                .getInstance(getContext())
                .getBoolean(
                        this.getContext().getString(R.string.pref_display_wheel_characters_key),
                        true);
        if (userPreferWheelLetters) {
            // Set the paint for drawing a nice background behind the current letter.
            float roundness = 25f;

            // Make the color the same as the typing trail, but blend it with white
            // because it's too hard to see a black font on dark backgrounds.
            int letterBackgroundColor = ColorUtils.blendARGB(typingTrailPaint.getColor(), Color.WHITE, 0.5f);

            letterBackgroundPaint.setColor(letterBackgroundColor);

            letterBackgroundOutlinePaint.setColor(Color.BLACK);
            letterBackgroundOutlinePaint.setStyle(Paint.Style.STROKE);
            letterBackgroundOutlinePaint.setStrokeWidth(3f);

            letterBackgroundPaint.setAntiAlias(true);
            letterBackgroundOutlinePaint.setAntiAlias(true);

            // Paint for the regular and bold fonts.
            foregroundPaint.setStrokeWidth(0.75f * density);
            foregroundPaint.setStyle(Paint.Style.FILL);
            foregroundPaint.setTextAlign(Paint.Align.CENTER);

            foregroundBoldPaint.setStrokeWidth(0.75f * density);
            foregroundBoldPaint.setStyle(Paint.Style.FILL);
            foregroundBoldPaint.setTextAlign(Paint.Align.CENTER);

            String characterSet = getCharacterSetToDisplay();
            for (int i = 0; i < characterSet.length(); i++) {
                Paint paint = foregroundPaint;
                String highlightedLetter = actionListener.getCurrentLetter();
                String letter = String.valueOf(characterSet.charAt(i));
                if (highlightedLetter != null && highlightedLetter.equalsIgnoreCase(letter)) {
                    paint = foregroundBoldPaint;

                    // Draw a box around the current letter.
                    float characterHeight = foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent;
                    float characterWidth = characterHeight;
                    canvas.drawRoundRect(
                            letterPositions[i * 2] - (characterWidth / 2), letterPositions[i * 2 + 1] - characterHeight,
                            letterPositions[i * 2] + (characterWidth / 2), letterPositions[i * 2 + 1] + (characterHeight / 2),
                            roundness, roundness, letterBackgroundPaint
                    );

                    canvas.drawRoundRect(
                            letterPositions[i * 2] - (characterWidth / 2), letterPositions[i * 2 + 1] - characterHeight,
                            letterPositions[i * 2] + (characterWidth / 2), letterPositions[i * 2 + 1] + (characterHeight / 2),
                            roundness, roundness, letterBackgroundOutlinePaint
                    );
                }
                canvas.drawText(letter, letterPositions[i * 2], letterPositions[i * 2 + 1], paint);
            }
        }
    }

    private void setForegroundPaint() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Regular.otf");
        Typeface fontBold = Typeface.createFromAsset(getContext().getAssets(),
                "SF-UI-Display-Bold.otf");

        foregroundPaint.setAntiAlias(true);
        foregroundPaint.setStrokeJoin(Paint.Join.ROUND);
        foregroundPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        foregroundPaint.setTypeface(font);

        foregroundBoldPaint.setAntiAlias(true);
        foregroundBoldPaint.setStrokeJoin(Paint.Join.ROUND);
        foregroundBoldPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));
        foregroundBoldPaint.setTypeface(fontBold);
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
        iconCenterY = centreYValue;
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
        iconCenterX = centreXValue;
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
        iconVectorDrawable.setBounds(coordinateX,
                coordinateY,
                coordinateX + iconSize,
                coordinateY + iconSize);
        iconVectorDrawable.setTint(foregroundColor);
        // TODO: define in .xml (don't know in which file)
        iconVectorDrawable.setAlpha(85);
        iconVectorDrawable.draw(canvas);
    }

    private void paintTypingTrail(Canvas canvas) {
        final short steps = 150;
        final byte stepDistance = 5;
        final byte maxTrailRadius = 14;
        pathMeasure.setPath(typingTrailPath, false);
        final float pathLength = pathMeasure.getLength();

        for (short i = 1; i <= steps; i++) {
            final float distance = pathLength - i * stepDistance;
            if (distance >= 0) {
                final float trailRadius = maxTrailRadius * (1 - (float) i / steps);
                pathMeasure.getPosTan(distance, trialPathPos, null);
                final float x = trialPathPos[0];
                final float y = trialPathPos[1];
                canvas.drawCircle(x, y, trailRadius, typingTrailPaint);
            }
        }
    }

    private int getRandomColor() {
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;
    }

    private String getCharacterSetToDisplay() {
        if (actionListener.areCharactersCapitalized() || actionListener.isCircleCapitalization()) {
            return actionListener.getUpperCaseCharacters();
        }

        return actionListener.getLowerCaseCharacters();
    }

    private FingerPosition getCurrentFingerPosition(PointF position) {
        if (circle.isPointInsideCircle(position)) {
            return FingerPosition.INSIDE_CIRCLE;
        } else {
            return circle.getSectorOfPoint(position);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        PointF position = new PointF((int) e.getX(), (int) e.getY());
        FingerPosition currentFingerPosition = getCurrentFingerPosition(position);
        invalidate();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionListener.movementStarted(currentFingerPosition);
                typingTrailPath.reset();
                typingTrailPath.moveTo(e.getX(), e.getY());
                if (userPreferRandomTrailColor) {
                    typingTrailPaint.setColor(getRandomColor());
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                actionListener.movementContinues(currentFingerPosition);
                typingTrailPath.lineTo(e.getX(), e.getY());
                return true;

            case MotionEvent.ACTION_UP:
                typingTrailPath.reset();
                actionListener.movementEnds();
                return true;

            case MotionEvent.ACTION_CANCEL:
                typingTrailPath.reset();
                actionListener.movementCanceled();
                return true;

            default:
                return false;
        }
    }

}
