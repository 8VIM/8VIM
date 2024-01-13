package inc.flide.vim8.ime.keyboard.xpad

import android.content.Context
import android.content.res.Configuration
import android.graphics.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.getOrNone
import arrow.core.raise.nullable
import arrow.core.raise.option
import arrow.core.recover
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.layout.loadKeyboardData
import inc.flide.vim8.ime.layout.models.CHARACTER_SET_SIZE
import inc.flide.vim8.ime.layout.models.Direction.Companion.baseQuadrant
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.MovementSequences
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.VisibleLayers
import inc.flide.vim8.ime.layout.models.MovementSequence
import inc.flide.vim8.ime.layout.models.MovementSequenceType
import inc.flide.vim8.ime.layout.models.characterSets
import inc.flide.vim8.ime.layout.models.findLayer
import inc.flide.vim8.ime.layout.models.toFingerPosition
import inc.flide.vim8.ime.layout.models.yaml.ExtraLayer
import inc.flide.vim8.ime.layout.safeLoadKeyboardData
import inc.flide.vim8.layoutLoader
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

private const val XPAD_CIRCLE_RADIUS_FACTOR = 40f
private const val XPAD_CIRCLE_OFFSET_FACTOR = 26

class Keyboard(private val context: Context) {
    companion object {
        val extraLayerMovementSequences = ExtraLayer.MOVEMENT_SEQUENCES.values.toSet()
    }

    private val prefs: AppPrefs by appPreferenceModel()
    private val layoutLoader by context.layoutLoader()
    var keyboardData: KeyboardData? by mutableStateOf(null)

    val keys = List(CHARACTER_SET_SIZE) { Key(it, this) }
    var trailColor: Color = Color.Unspecified
    var layerLevel: LayerLevel by mutableStateOf(LayerLevel.FIRST)
    var lengthOfLineDemarcatingSectors = 0f
        private set
    val circle = Circle()
    private val isTabletLandscape: Boolean
        get() =
            context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                context.resources.configuration.screenHeightDp >= 480

    init {
        keyboardData = safeLoadKeyboardData(layoutLoader, context)!!
        prefs.layout.current.observe {
            it.loadKeyboardData(layoutLoader, context)
                .onRight { keyboardData ->
                    this.keyboardData = keyboardData
                }
        }
    }

    fun reset() {
        layerLevel = LayerLevel.FIRST
    }

    fun findLayer(movementSequence: MutableList<FingerPosition>) {
        keyboardData?.let {
            for (i in VisibleLayers.size - 1 downTo LayerLevel.SECOND.ordinal) {
                val layerLevel = LayerLevel.entries[i]
                val extraLayerMovementSequence = MovementSequences[layerLevel]
                if (extraLayerMovementSequence == null) {
                    this.layerLevel = LayerLevel.FIRST
                    return
                }
                if (movementSequence.size < extraLayerMovementSequence.size) {
                    continue
                }
                val startWith: MovementSequence =
                    movementSequence.subList(0, extraLayerMovementSequence.size)

                if (extraLayerMovementSequences.contains(startWith) &&
                    layerLevel.ordinal <= it.totalLayers
                ) {
                    this.layerLevel = layerLevel
                    return
                }
            }

            this.layerLevel = it
                .findLayer(movementSequence + FingerPosition.INSIDE_CIRCLE)
        }
    }

    fun key(movementSequence: MovementSequence): Key? = nullable {
        val keyboardData = ensureNotNull(keyboardData)
        val keyboardAction = keyboardData.actionMap.getOrNone(movementSequence).bind()
        val characterSet = keyboardData.characterSets(layerLevel).bind()
        keys.firstOrNone { key ->
            characterSet[key.index] == keyboardAction
        }.bind()
    }

    fun action(
        movementSequence: MovementSequence,
        currentMovementSequenceType: MovementSequenceType
    ): Option<KeyboardAction> = option {
        val keyboardData = ensureNotNull(keyboardData)
        keyboardData.actionMap
            .getOrNone(movementSequence)
            .recover {
                keyboardData.actionMap
                    .getOrNone(listOf(FingerPosition.NO_TOUCH) + movementSequence)
                    .filter { currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT }
                    .bind()
            }.bind()
    }

    fun hasAction(movementSequence: MovementSequence): Boolean =
        keyboardData?.actionMap?.containsKey(movementSequence) ?: false

    fun layout(
        size: Size,
        isSidebarOnLeft: Boolean,
        radiusSizeFactor: Int,
        xCentreOffset: Int,
        yCentreOffset: Int,
        textMeasurer: TextMeasurer,
        textStyle: TextStyle
    ) {
        val keyboardHeight = size.height
        val characterHeight = textMeasurer.measure("A", textStyle).size.height.toFloat()
        val spRadiusValue = radiusSizeFactor.toFloat()
        val radius = (spRadiusValue / XPAD_CIRCLE_RADIUS_FACTOR * keyboardHeight) / 2f
        val offsetX = xCentreOffset * XPAD_CIRCLE_OFFSET_FACTOR
        val offsetY = yCentreOffset * XPAD_CIRCLE_OFFSET_FACTOR

        val smallDim = min(
            if (offsetX > 0) size.width / 2 - offsetX else size.width / 2 + offsetX,
            size.height / 2 - abs(offsetY)
        )
        lengthOfLineDemarcatingSectors = hypot(smallDim, smallDim) - radius - characterHeight

        val x = if (isTabletLandscape) {
            val base = lengthOfLineDemarcatingSectors + offsetX
            if (!isSidebarOnLeft) {
                size.width - base
            } else {
                base
            }
        } else {
            size.width / 2f + offsetX
        }

        val circleCenter = Offset(x, y = size.height / 2f + offsetY)
        circle.centre = circleCenter
        circle.radius = radius

        val letterPositions = List(4 * 2 * 4 * 2) { 0f }.toFloatArray()
        val matrix = Matrix()
        val eastEdge = circleCenter.x + radius + characterHeight / 2f
        for (i in 0 until 4) {
            val dx = i * lengthOfLineDemarcatingSectors / 4f
            letterPositions[4 * i] = eastEdge + dx
            letterPositions[4 * i + 1] = circleCenter.y - characterHeight / 2f
            letterPositions[4 * i + 2] = eastEdge + dx
            letterPositions[4 * i + 3] = circleCenter.y + characterHeight / 2f
        }

        matrix.reset()
        matrix.postRotate(45f, circleCenter.x, circleCenter.y)
        matrix.mapPoints(letterPositions, 0, letterPositions, 0, 8)
        matrix.reset()
        matrix.postRotate(90f, circleCenter.x, circleCenter.y)

        for (i in 1 until 4) {
            matrix.mapPoints(
                letterPositions,
                4 * 4 * i,
                letterPositions,
                4 * 4 * (i - 1),
                8
            )
        }
        matrix.reset()
        matrix.postTranslate(-characterHeight / 3f, -characterHeight / 2f)
        matrix.mapPoints(letterPositions)
        keys.withIndex().forEach { (i, key) ->
            key.position = Offset(letterPositions[i * 2], letterPositions[i * 2 + 1])
        }
    }

    data class Circle(var centre: Offset = Offset.Zero, var radius: Float = 0f) {

        private fun getPowerOfPoint(point: Offset): Float {
            /*
            If O is the centre of circle
            Consider startingPoint point P not necessarily on the circumference of the circle.
            If d = OP is the distance between P and the circle's center O,
            then the power of the point P relative to the circle is
            p=d^2-r^2.
            */
            val squaredDistanceBetweenPoints = (point - centre).getDistanceSquared()
            val radiusSquare = radius * radius
            return squaredDistanceBetweenPoints - radiusSquare
        }

        fun isPointInsideCircle(point: Offset): Boolean {
            return getPowerOfPoint(point) < 0
        }

        private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: Offset): Float {
            // Get difference of coordinates
            val x = (point.x - centre.x).toDouble()
            val y = (centre.y - point.y).toDouble()

            // Calculate angle with special atan (calculates the correct angle in all quadrants)
            var angle = atan2(y, x)
            // Make all angles positive
            if (angle < 0) {
                angle += Math.PI * 2
            }
            return angle.toFloat()
        }

        /**
         * Get the number of the sector that point p is in
         */
        fun getSectorOfPoint(p: Offset): FingerPosition {
            val angleDouble = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
            val angleToSectorValue = angleDouble / (Math.PI / 2)
            val quadrantCyclic = angleToSectorValue.roundToInt()
            return baseQuadrant(quadrantCyclic).toFingerPosition()
        }
    }
}
