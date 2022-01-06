package inc.flide.vim8.views.mainKeyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.R
import inc.flide.vim8.geometry.Circle
import inc.flide.vim8.geometry.Dimension
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.FingerPosition
import java.util.*

class XpadView : View {
    private val rnd: Random? = Random()
    private val typingTrailPath: Path? = Path()
    private val backgroundPaint: Paint? = Paint()
    private val foregroundPaint: Paint? = Paint()
    private val typingTrailPaint: Paint? = Paint()
    private var actionListener: MainKeypadActionListener? = null
    private var circleCenter: PointF? = null
    private var circle: Circle? = null
    private val keypadDimension: Dimension? = Dimension()
    private val xformMatrix: Matrix? = Matrix()

    // There are 4 sectors, each has 4 letters above, and 4 below.
    // Finally, each letter position has an x & y co-ordinate.
    private val letterPositions: FloatArray? = FloatArray(4 * 2 * 4 * 2)
    private val sectorLines: Path? = Path()
    private val sectorLineBounds: RectF? = RectF()
    private var backgroundColor = 0
    private var foregroundColor = 0
    private var trailColor = 0
    private val trialPathPos: FloatArray? = FloatArray(2)
    private val pathMeasure: PathMeasure? = PathMeasure()
    private var userPreferRandomTrailColor = false

    constructor(context: Context?) : super(context) {
        initialize(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    private fun updateColors(context: Context?) {
        val resources = resources
        val sharedPreferenceHelper: SharedPreferenceHelper = SharedPreferenceHelper.Companion.getInstance(context)
        backgroundColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_board_bg_color_key),
                resources.getColor(R.color.defaultBoardBg))
        foregroundColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg))
        userPreferRandomTrailColor = sharedPreferenceHelper.getBoolean(
                resources.getString(R.string.pref_random_trail_color_key),
                false)
        trailColor = sharedPreferenceHelper.getInt(
                resources.getString(R.string.pref_trail_color_key),
                resources.getColor(R.color.defaultTrail))
        backgroundPaint.setColor(backgroundColor)
        foregroundPaint.setColor(foregroundColor)
        typingTrailPaint.setColor(trailColor)
    }

    private fun initialize(context: Context?) {
        SharedPreferenceHelper.Companion.getInstance(context).addListener(SharedPreferenceHelper.Listener {
            updateColors(context)
            computeComponentPositions(this.width, this.height)
            this.invalidate()
        })
        updateColors(context)
        setForegroundPaint()
        actionListener = MainKeypadActionListener(context as MainInputMethodService?, this)
        isHapticFeedbackEnabled = true
        circleCenter = PointF()
        circle = Circle()
    }

    private fun computeComponentPositions(fullWidth: Int, fullHeight: Int) {
        val context = context
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.Companion.getInstance(context)
        val spRadiusValue = pref.getInt(context.getString(R.string.pref_circle_scale_factor), 3).toFloat()
        // TODO: Store constant in .xml file (but where?)
        val radius = spRadiusValue / 40f * keypadDimension.getWidth() / 2
        val xOffset = pref.getInt(context.getString(R.string.pref_circle_x_offset_key), 0) * 26
        val yOffset = pref.getInt(context.getString(R.string.pref_circle_y_offset_key), 0) * 26
        circleCenter.x = keypadDimension.getWidth() / 2f + xOffset
        circleCenter.y = keypadDimension.getHeight() / 2f + yOffset
        circle.setCentre(circleCenter)
        circle.setRadius(radius)
        val characterHeight = foregroundPaint.getFontMetrics().descent - foregroundPaint.getFontMetrics().ascent
        // We chop off a bit of the right side of the view width from the keypadDimension (see onMeasure),
        // this introduces a bit of asymmetry which we have to compensate for here.
        val keypadXOffset = fullWidth - keypadDimension.getWidth()
        // If the xOffset is to the right, we can spread into the extra padding space.
        val smallDim = Math.min(if (xOffset > 0) fullWidth / 2 - xOffset + keypadXOffset // If xOffset goes to the left, restrict to keypadDimension.
        else keypadDimension.getWidth() / 2 + xOffset,
                fullHeight / 2 - Math.abs(yOffset))
        // Compute the length of sector lines, such that they stop a little before hitting the edge of the view.
        val lengthOfLineDemarcatingSectors = (Math.hypot(smallDim.toDouble(), smallDim.toDouble()) as Float
                - radius - characterHeight)

        // Compute sector demarcation lines as if they were all going orthogonal (like a "+").
        // This is easier to compute.  Later we apply rotation to orient the lines properly (like an "x").
        sectorLines.rewind()
        sectorLines.moveTo(circleCenter.x + radius, circleCenter.y)
        sectorLines.rLineTo(lengthOfLineDemarcatingSectors, 0f)
        sectorLines.moveTo(circleCenter.x - radius, circleCenter.y)
        sectorLines.rLineTo(-lengthOfLineDemarcatingSectors, 0f)
        sectorLines.moveTo(circleCenter.x, circleCenter.y + radius)
        sectorLines.rLineTo(0f, lengthOfLineDemarcatingSectors)
        sectorLines.moveTo(circleCenter.x, circleCenter.y - radius)
        sectorLines.rLineTo(0f, -lengthOfLineDemarcatingSectors)

        // Compute the first set of points going straight to the "east" (aka, rightwards).
        // Then apply repeated rotation (45, then 90 x4) to get the final positions.
        val eastEdge = circleCenter.x + circle.getRadius() + characterHeight / 2
        for (i in 0..3) {
            val dx = i * lengthOfLineDemarcatingSectors / 4f
            letterPositions.get(4 * i) = eastEdge + dx
            letterPositions.get(4 * i + 1) = circleCenter.y - characterHeight / 2 // upper letter
            letterPositions.get(4 * i + 2) = eastEdge + dx
            letterPositions.get(4 * i + 3) = circleCenter.y + characterHeight / 2 // lower letter
        }
        xformMatrix.reset()
        xformMatrix.postRotate(45f, circleCenter.x, circleCenter.y)
        xformMatrix.mapPoints(letterPositions, 0, letterPositions, 0, 8)
        sectorLines.transform(xformMatrix)
        xformMatrix.reset()
        xformMatrix.postRotate(90f, circleCenter.x, circleCenter.y)
        for (i in 1..3) {
            xformMatrix.mapPoints(letterPositions, 4 * 4 * i, letterPositions, 4 * 4 * (i - 1), 8)
        }

        // Canvas.drawPosText() draws from the bottom,
        // so we need to offset downwards a bit to compensate.
        xformMatrix.reset()
        xformMatrix.postTranslate(0f, 3 * characterHeight / 16)
        xformMatrix.mapPoints(letterPositions)
        sectorLines.computeBounds(sectorLineBounds, false) // Used to position icons
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        keypadDimension.setWidth(parentWidth / 6 * 5)
        keypadDimension.setHeight(parentHeight)
        setMeasuredDimension(keypadDimension.getWidth(), keypadDimension.getHeight())
        // this.getWidth() returns 0 at this point, parentWidth (& height) give the correct result.
        computeComponentPositions(parentWidth, parentHeight)
    }

    public override fun onDraw(canvas: Canvas?) {
        canvas.drawColor(backgroundPaint.getColor())
        val userPrefersTypingTrail: Boolean = SharedPreferenceHelper.Companion.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_typing_trail_visibility_key),
                        true)
        if (userPrefersTypingTrail) {
            paintTypingTrail(canvas)
        }
        val density = resources.displayMetrics.density
        foregroundPaint.setStrokeWidth(2 * density)
        foregroundPaint.setStyle(Paint.Style.STROKE)

        //The centre circle
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(), foregroundPaint)
        canvas.drawPath(sectorLines, foregroundPaint) //The lines demarcating the sectors

        // Converting float value to int
        val centreXValue = circle.getCentre().x as Int
        val centreYValue = circle.getCentre().y as Int
        val userPrefersSectorIcons: Boolean = SharedPreferenceHelper.Companion.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_display_sector_icons_key),
                        true)
        if (userPrefersSectorIcons) {
            setupSectorIcons(centreXValue, centreYValue, canvas)
        }

        //the text along the lines
        val userPreferWheelLetters: Boolean = SharedPreferenceHelper.Companion.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_display_wheel_characters_key),
                        true)
        if (userPreferWheelLetters) {
            foregroundPaint.setStrokeWidth(0.75f * density)
            foregroundPaint.setStyle(Paint.Style.FILL)
            foregroundPaint.setTextAlign(Paint.Align.CENTER)
            canvas.drawPosText(getCharacterSetToDisplay(), letterPositions, foregroundPaint)
        }
    }

    private fun setForegroundPaint() {
        foregroundPaint.setAntiAlias(true)
        foregroundPaint.setStrokeJoin(Paint.Join.ROUND)
        foregroundPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.font_size).toFloat())
        val font = Typeface.createFromAsset(context.assets,
                "SF-UI-Display-Regular.otf")
        foregroundPaint.setTypeface(font)
    }

    private fun setupSectorIcons(centreXValue: Int, centreYValue: Int, canvas: Canvas?) {
        val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
        val iconHalfWidth = iconSize / 2
        val iconHalfHeight = iconSize / 2
        sectorLines.computeBounds(sectorLineBounds, false)
        //Number pad icon (left side)
        var iconCenterX = Math.max(sectorLineBounds.left, 0f) as Int
        var iconCenterY = centreYValue
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.numericpad_vd_vector)

        //for Backspace icon (right side)
        iconCenterX = Math.min(sectorLineBounds.right, canvas.getWidth().toFloat()) as Int
        iconCenterY = centreYValue
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_backspace)

        //for Enter icon (bottom)
        iconCenterX = centreXValue
        iconCenterY = Math.min(sectorLineBounds.bottom, canvas.getHeight().toFloat()) as Int
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_keyboard_return)

        //for caps lock and shift icon
        iconCenterX = centreXValue
        iconCenterY = Math.max(sectorLineBounds.top, 0f) as Int
        var shiftIconToDisplay = R.drawable.ic_no_capslock
        if (actionListener.isShiftSet()) {
            shiftIconToDisplay = R.drawable.ic_shift_engaged
        }
        if (actionListener.isCapsLockSet()) {
            shiftIconToDisplay = R.drawable.ic_capslock_engaged
        }
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                shiftIconToDisplay)
    }

    private fun drawIconInSector(coordinateX: Int, coordinateY: Int, canvas: Canvas?, resourceId: Int) {
        val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
        val iconVectorDrawable = VectorDrawableCompat
                .create(context.resources, resourceId, null)
        iconVectorDrawable.setBounds(coordinateX,
                coordinateY,
                coordinateX + iconSize,
                coordinateY + iconSize)
        iconVectorDrawable.setTint(foregroundColor)
        // TODO: define in .xml (don't know in which file)
        iconVectorDrawable.setAlpha(85)
        iconVectorDrawable.draw(canvas)
    }

    private fun paintTypingTrail(canvas: Canvas?) {
        val steps: Short = 150
        val stepDistance: Byte = 5
        val maxTrailRadius: Byte = 14
        pathMeasure.setPath(typingTrailPath, false)
        val pathLength = pathMeasure.getLength()
        for (i in 1..steps) {
            val distance = pathLength - i * stepDistance
            if (distance >= 0) {
                val trailRadius = maxTrailRadius * (1 - i as Float / steps)
                pathMeasure.getPosTan(distance, trialPathPos, null)
                val x = trialPathPos.get(0)
                val y = trialPathPos.get(1)
                canvas.drawCircle(x, y, trailRadius, typingTrailPaint)
            }
        }
    }

    private fun getRandomColor(): Int {
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun getCharacterSetToDisplay(): String? {
        return if (actionListener.areCharactersCapitalized()) {
            actionListener.getUpperCaseCharacters()
        } else actionListener.getLowerCaseCharacters()
    }

    private fun getCurrentFingerPosition(position: PointF?): FingerPosition? {
        return if (circle.isPointInsideCircle(position)) {
            FingerPosition.INSIDE_CIRCLE
        } else {
            circle.getSectorOfPoint(position)
        }
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val position = PointF(e.getX() as Int, e.getY() as Int)
        val currentFingerPosition = getCurrentFingerPosition(position)
        invalidate()
        return when (e.getActionMasked()) {
            MotionEvent.ACTION_DOWN -> {
                actionListener.movementStarted(currentFingerPosition)
                typingTrailPath.reset()
                typingTrailPath.moveTo(e.getX(), e.getY())
                if (userPreferRandomTrailColor) {
                    typingTrailPaint.setColor(getRandomColor())
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                actionListener.movementContinues(currentFingerPosition)
                typingTrailPath.lineTo(e.getX(), e.getY())
                true
            }
            MotionEvent.ACTION_UP -> {
                typingTrailPath.reset()
                actionListener.movementEnds()
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                typingTrailPath.reset()
                actionListener.movementCanceled()
                true
            }
            else -> false
        }
    }
}