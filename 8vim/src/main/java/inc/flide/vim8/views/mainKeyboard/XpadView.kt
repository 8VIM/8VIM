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
import kotlin.math.*

class XpadView : View {
    private val rnd: Random = Random()
    private val typingTrailPath: Path = Path()
    private val backgroundPaint: Paint = Paint()
    private val foregroundPaint: Paint = Paint()
    private val typingTrailPaint: Paint = Paint()
    private lateinit var actionListener: MainKeypadActionListener
    private lateinit var circleCenter: PointF
    private lateinit var circle: Circle
    private val keypadDimension: Dimension = Dimension()
    private val xFormMatrix: Matrix = Matrix()

    // There are 4 sectors, each has 4 letters above, and 4 below.
    // Finally, each letter position has an x & y co-ordinate.
    private val letterPositions: FloatArray = FloatArray(4 * 2 * 4 * 2)
    private val sectorLines: Path = Path()
    private val sectorLineBounds: RectF = RectF()
    private var bgColor = 0
    private var foregroundColor = 0
    private var trailColor = 0
    private val trialPathPos: FloatArray = FloatArray(2)
    private val pathMeasure: PathMeasure = PathMeasure()
    private var userPreferRandomTrailColor = false

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    private fun updateColors(context: Context) {
        val resources = resources
        val sharedPreferenceHelper: SharedPreferenceHelper = SharedPreferenceHelper.getInstance(context)
        bgColor = sharedPreferenceHelper.getInt(
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
        backgroundPaint.color = bgColor
        foregroundPaint.color = foregroundColor
        typingTrailPaint.color = trailColor
    }

    private fun initialize(context: Context) {
        SharedPreferenceHelper.getInstance(context).addListener(
            object : SharedPreferenceHelper.Listener() {
                override fun onPreferenceChanged() {
                    updateColors(context)
                    computeComponentPositions(width, height)
                    invalidate()
                }
        })
        updateColors(context)
        setForegroundPaint()
        actionListener = MainKeypadActionListener(context as MainInputMethodService, this)
        isHapticFeedbackEnabled = true
        circleCenter = PointF()
        circle = Circle()
    }

    private fun computeComponentPositions(fullWidth: Int, fullHeight: Int) {
        val context = context
        val pref: SharedPreferenceHelper = SharedPreferenceHelper.getInstance(context)
        val spRadiusValue = pref.getInt(context.getString(R.string.pref_circle_scale_factor), 3).toFloat()
        // TODO: Store constant in .xml file (but where?)
        val radius = spRadiusValue / 40f * keypadDimension.getWidth() / 2
        val xOffset = pref.getInt(context.getString(R.string.pref_circle_x_offset_key), 0) * 26
        val yOffset = pref.getInt(context.getString(R.string.pref_circle_y_offset_key), 0) * 26
        circleCenter.x = keypadDimension.getWidth() / 2f + xOffset
        circleCenter.y = keypadDimension.getHeight() / 2f + yOffset
        circle.setCentre(circleCenter)
        circle.setRadius(radius)
        val characterHeight = foregroundPaint.fontMetrics.descent - foregroundPaint.fontMetrics.ascent
        // We chop off a bit of the right side of the view width from the keypadDimension (see onMeasure),
        // this introduces a bit of asymmetry which we have to compensate for here.
        val keypadXOffset = fullWidth - keypadDimension.getWidth()
        // If the xOffset is to the right, we can spread into the extra padding space.
        val smallDim = min(if (xOffset > 0) fullWidth / 2 - xOffset + keypadXOffset // If xOffset goes to the left, restrict to keypadDimension.
        else keypadDimension.getWidth() / 2 + xOffset,
                fullHeight / 2 - abs(yOffset)
        )
        // Compute the length of sector lines, such that they stop a little before hitting the edge of the view.
        val lengthOfLineDemarcatingSectors = (hypot(smallDim.toDouble(), smallDim.toDouble()).toFloat()
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
            letterPositions[4 * i] = eastEdge + dx
            letterPositions[4 * i + 1] = circleCenter.y - characterHeight / 2 // upper letter
            letterPositions[4 * i + 2] = eastEdge + dx
            letterPositions[4 * i + 3] = circleCenter.y + characterHeight / 2 // lower letter
        }
        xFormMatrix.reset()
        xFormMatrix.postRotate(45f, circleCenter.x, circleCenter.y)
        xFormMatrix.mapPoints(letterPositions, 0, letterPositions, 0, 8)
        sectorLines.transform(xFormMatrix)
        xFormMatrix.reset()
        xFormMatrix.postRotate(90f, circleCenter.x, circleCenter.y)
        for (i in 1..3) {
            xFormMatrix.mapPoints(letterPositions, 4 * 4 * i, letterPositions, 4 * 4 * (i - 1), 8)
        }

        // Canvas.drawPosText() draws from the bottom,
        // so we need to offset downwards a bit to compensate.
        xFormMatrix.reset()
        xFormMatrix.postTranslate(0f, 3 * characterHeight / 16)
        xFormMatrix.mapPoints(letterPositions)
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

    public override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundPaint.color)
        val userPrefersTypingTrail: Boolean = SharedPreferenceHelper.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_typing_trail_visibility_key),
                        true)
        if (userPrefersTypingTrail) {
            paintTypingTrail(canvas)
        }
        val density = resources.displayMetrics.density
        foregroundPaint.strokeWidth = 2 * density
        foregroundPaint.style = Paint.Style.STROKE

        //The centre circle
        canvas.drawCircle(circle.getCentre().x, circle.getCentre().y, circle.getRadius(), foregroundPaint)
        canvas.drawPath(sectorLines, foregroundPaint) //The lines demarcating the sectors

        // Converting float value to int
        val centreXValue = circle.getCentre().x.toInt()
        val centreYValue = circle.getCentre().y.toInt()
        val userPrefersSectorIcons: Boolean = SharedPreferenceHelper.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_display_sector_icons_key),
                        true)
        if (userPrefersSectorIcons) {
            setupSectorIcons(centreXValue, centreYValue, canvas)
        }

        //the text along the lines
        val userPreferWheelLetters: Boolean = SharedPreferenceHelper.getInstance(context)
                .getBoolean(
                        this.context.getString(R.string.pref_display_wheel_characters_key),
                        true)
        if (userPreferWheelLetters) {
            foregroundPaint.strokeWidth = 0.75f * density
            foregroundPaint.style = Paint.Style.FILL
            foregroundPaint.textAlign = Paint.Align.CENTER
            canvas.drawPosText(getCharacterSetToDisplay(), letterPositions, foregroundPaint)
        }
    }

    private fun setForegroundPaint() {
        foregroundPaint.isAntiAlias = true
        foregroundPaint.strokeJoin = Paint.Join.ROUND
        foregroundPaint.textSize = resources.getDimensionPixelSize(R.dimen.font_size).toFloat()
        val font = Typeface.createFromAsset(context.assets,
                "SF-UI-Display-Regular.otf")
        foregroundPaint.typeface = font
    }

    private fun setupSectorIcons(centreXValue: Int, centreYValue: Int, canvas: Canvas) {
        val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
        val iconHalfWidth = iconSize / 2
        val iconHalfHeight = iconSize / 2
        sectorLines.computeBounds(sectorLineBounds, false)
        //Number pad icon (left side)
        var iconCenterX = max(sectorLineBounds.left, 0f).toInt()
        var iconCenterY = centreYValue
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.numericpad_vd_vector)

        //for Backspace icon (right side)
        iconCenterX = min(sectorLineBounds.right, canvas.width.toFloat()).toInt()
        iconCenterY = centreYValue
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_backspace)

        //for Enter icon (bottom)
        iconCenterX = centreXValue
        iconCenterY = min(sectorLineBounds.bottom, canvas.height.toFloat()).toInt()
        drawIconInSector(iconCenterX - iconHalfWidth,
                iconCenterY - iconHalfHeight,
                canvas,
                R.drawable.ic_keyboard_return)

        //for caps lock and shift icon
        iconCenterX = centreXValue
        iconCenterY = max(sectorLineBounds.top, 0f).toInt()
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

    private fun drawIconInSector(coordinateX: Int, coordinateY: Int, canvas: Canvas, resourceId: Int) {
        val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
        val iconVectorDrawable = VectorDrawableCompat
                .create(context.resources, resourceId, null)
        iconVectorDrawable!!.setBounds(coordinateX,
                coordinateY,
                coordinateX + iconSize,
                coordinateY + iconSize)
        iconVectorDrawable.setTint(foregroundColor)
        // TODO: define in .xml (don't know in which file)
        iconVectorDrawable.alpha = 85
        iconVectorDrawable.draw(canvas)
    }

    private fun paintTypingTrail(canvas: Canvas) {
        val steps: Short = 150
        val stepDistance: Byte = 5
        val maxTrailRadius: Byte = 14
        pathMeasure.setPath(typingTrailPath, false)
        val pathLength = pathMeasure.length
        for (i in 1..steps) {
            val distance = pathLength - i * stepDistance
            if (distance >= 0) {
                val trailRadius = maxTrailRadius * (1 - i / steps)
                pathMeasure.getPosTan(distance, trialPathPos, null)
                val x = trialPathPos[0]
                val y = trialPathPos[1]
                canvas.drawCircle(x, y, trailRadius.toFloat(), typingTrailPaint)
            }
        }
    }

    private fun getRandomColor(): Int {
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun getCharacterSetToDisplay(): String {
        return if (actionListener.areCharactersCapitalized()) {
            actionListener.getUpperCaseCharacters()
        } else actionListener.getLowerCaseCharacters()
    }

    private fun getCurrentFingerPosition(position: PointF): FingerPosition {
        return if (circle.isPointInsideCircle(position)) {
            FingerPosition.INSIDE_CIRCLE
        } else {
            circle.getSectorOfPoint(position)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val position = PointF(floor(e.x), floor(e.y))
        val currentFingerPosition = getCurrentFingerPosition(position)
        invalidate()
        return when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                actionListener.movementStarted(currentFingerPosition)
                typingTrailPath.reset()
                typingTrailPath.moveTo(e.x, e.y)
                if (userPreferRandomTrailColor) {
                    typingTrailPaint.color = getRandomColor()
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                actionListener.movementContinues(currentFingerPosition)
                typingTrailPath.lineTo(e.x, e.y)
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