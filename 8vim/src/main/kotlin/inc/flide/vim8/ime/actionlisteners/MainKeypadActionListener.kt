package inc.flide.vim8.ime.actionlisteners

import android.os.Handler
import android.os.HandlerThread
import android.view.View
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.getOrNone
import arrow.core.recover
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.MovementSequences
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.VisibleLayers
import inc.flide.vim8.ime.layout.models.MovementSequence
import inc.flide.vim8.ime.layout.models.MovementSequenceType
import inc.flide.vim8.ime.layout.models.findLayer
import inc.flide.vim8.ime.layout.models.lowerCaseCharacters
import inc.flide.vim8.ime.layout.models.upperCaseCharacters
import inc.flide.vim8.ime.layout.models.yaml.ExtraLayer

class MainKeypadActionListener(inputMethodService: Vim8ImeService, view: View) :
    KeypadActionListener(inputMethodService, view) {
    companion object {
        private const val DELAY_MILLIS_LONG_PRESS_CONTINUATION = 50
        private const val DELAY_MILLIS_LONG_PRESS_INITIATION = 500
        private const val FULL_ROTATION_STEPS = 7
        private val extraLayerMovementSequences = ExtraLayer.MOVEMENT_SEQUENCES.values.toSet()
        private val ROTATION_MOVEMENT_SEQUENCES = setOf(
            listOf(
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.TOP,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT
            ),
            listOf(
                FingerPosition.BOTTOM,
                FingerPosition.RIGHT,
                FingerPosition.TOP,
                FingerPosition.LEFT,
                FingerPosition.BOTTOM,
                FingerPosition.RIGHT
            ),
            listOf(
                FingerPosition.LEFT,
                FingerPosition.TOP,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.TOP
            ),
            listOf(
                FingerPosition.LEFT,
                FingerPosition.BOTTOM,
                FingerPosition.RIGHT,
                FingerPosition.TOP,
                FingerPosition.LEFT,
                FingerPosition.BOTTOM
            ),
            listOf(
                FingerPosition.TOP,
                FingerPosition.LEFT,
                FingerPosition.BOTTOM,
                FingerPosition.RIGHT,
                FingerPosition.TOP,
                FingerPosition.LEFT
            ),
            listOf(
                FingerPosition.TOP,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.TOP,
                FingerPosition.RIGHT
            ),
            listOf(
                FingerPosition.RIGHT,
                FingerPosition.TOP,
                FingerPosition.LEFT,
                FingerPosition.BOTTOM,
                FingerPosition.RIGHT,
                FingerPosition.TOP
            ),
            listOf(
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM,
                FingerPosition.LEFT,
                FingerPosition.TOP,
                FingerPosition.RIGHT,
                FingerPosition.BOTTOM
            )
        )
        private var keyboardData: Option<KeyboardData> = None

        @JvmStatic
        fun rebuildKeyboardData(keyboardDataOption: KeyboardData?) {
            keyboardData = Option.fromNullable(keyboardDataOption)
        }
    }

    private val movementSequence: MutableList<FingerPosition> = arrayListOf()
    private lateinit var longPressHandler: Handler
    private var currentFingerPosition: FingerPosition
    var currentLetter: String? = null
        private set
    private var isLongPressCallbackSet = false
    private var currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    private val longPressRunnable: Runnable = object : Runnable {
        override fun run() {
            val movementSequenceAugmented: MutableList<FingerPosition> = ArrayList(movementSequence)
            movementSequenceAugmented.add(FingerPosition.LONG_PRESS)
            processMovementSequence(movementSequenceAugmented)
            longPressHandler.postDelayed(this, DELAY_MILLIS_LONG_PRESS_CONTINUATION.toLong())
        }
    }

    init {
        currentFingerPosition = FingerPosition.NO_TOUCH
        val longPressHandlerThread = HandlerThread("LongPressHandlerThread")
        longPressHandlerThread.start()
        longPressHandler = Handler(longPressHandlerThread.looper, null)
    }

    fun getLowerCaseCharacters(layer: LayerLevel): String {
        return keyboardData.flatMap { it.lowerCaseCharacters(layer) }.getOrElse { "" }
    }

    fun getUpperCaseCharacters(layer: LayerLevel): String {
        return keyboardData.flatMap { it.upperCaseCharacters(layer) }.getOrElse { "" }
    }

    override fun findLayer(): LayerLevel {
        for (i in VisibleLayers.size - 1 downTo LayerLevel.SECOND.ordinal) {
            val layerLevel = LayerLevel.values()[i]
            val extraLayerMovementSequence = MovementSequences[layerLevel]
                ?: return LayerLevel.FIRST
            if (movementSequence.size < extraLayerMovementSequence.size) {
                continue
            }
            val startWith: MovementSequence =
                movementSequence.subList(0, extraLayerMovementSequence.size)
            if (keyboardData.isSome {
                extraLayerMovementSequences.contains(startWith) &&
                    layerLevel.ordinal <= it.totalLayers
            }
            ) {
                return layerLevel
            }
        }
        return keyboardData
            .map { it.findLayer(movementSequence + FingerPosition.INSIDE_CIRCLE) }
            .getOrElse { LayerLevel.FIRST }
    }

    private val isFullRotation: Boolean
        get() {
            val layer = findLayer()
            var size = FULL_ROTATION_STEPS
            var start = 1
            var layerCondition = movementSequence[0] == FingerPosition.INSIDE_CIRCLE
            if (layer !== LayerLevel.FIRST) {
                MovementSequences.getOrNone(layer).onSome {
                    size += it.size
                    start += it.size
                    layerCondition = extraLayerMovementSequences.contains(
                        movementSequence.subList(0, it.size)
                    )
                }
            }
            return if (movementSequence.size == size && layerCondition) {
                ROTATION_MOVEMENT_SEQUENCES.contains(
                    movementSequence.subList(start, size)
                )
            } else {
                false
            }
        }

    fun movementStarted(fingerPosition: FingerPosition) {
        currentFingerPosition = fingerPosition
        movementSequence.clear()
        currentLetter = null
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
        movementSequence.add(currentFingerPosition)
        initiateLongPressDetection()
    }

    fun movementContinues(fingerPosition: FingerPosition) {
        val lastKnownFingerPosition = currentFingerPosition
        currentFingerPosition = fingerPosition
        val isFingerPositionChanged = lastKnownFingerPosition !== currentFingerPosition
        if (isFingerPositionChanged) {
            interruptLongPress()
            movementSequence.add(currentFingerPosition)
            if (isFullRotation) {
                var start = 2
                var size = FULL_ROTATION_STEPS - 1
                val layer = findLayer()
                if (layer !== LayerLevel.FIRST) {
                    MovementSequences.getOrNone(layer).onSome {
                        start += it.size
                        size += it.size
                    }
                }
                movementSequence.subList(start, size).clear()
                vim8ImeService.performShiftToggle()
            }
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE &&
                keyboardData.isSome { it.actionMap.containsKey(movementSequence) }
            ) {
                processMovementSequence(movementSequence)
                movementSequence.clear()
                currentLetter = null
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT
                movementSequence.add(currentFingerPosition)
            } else if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE) {
                val layer = findLayer()
                var layerSize = 0
                val extraLayerMovementSequences = if (layer !== LayerLevel.FIRST) {
                    MovementSequences.getOrNone(layer)
                        .onSome {
                            layerSize = it.size + 1
                        }.getOrElse { listOf() }
                } else {
                    listOf()
                }
                val defaultLayerCondition = (
                    layer == LayerLevel.FIRST &&
                        movementSequence[0] == FingerPosition.INSIDE_CIRCLE
                    )
                val extraLayerCondition =
                    layer !== LayerLevel.FIRST && movementSequence.size > layerSize
                if (defaultLayerCondition || extraLayerCondition) {
                    movementSequence.clear()
                    currentLetter = null
                    currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
                    movementSequence.addAll(extraLayerMovementSequences)
                    movementSequence.add(currentFingerPosition)
                }
            } else {
                val modifiedMovementSequence =
                    movementSequence + FingerPosition.INSIDE_CIRCLE
                keyboardData
                    .flatMap { it.actionMap.getOrNone(modifiedMovementSequence) }
                    .onSome {
                        currentLetter = if (areCharactersCapitalized()) it.capsLockText else it.text
                    }
            }
        } else if (!isLongPressCallbackSet) {
            initiateLongPressDetection()
        }
    }

    fun movementEnds() {
        interruptLongPress()
        currentFingerPosition = FingerPosition.NO_TOUCH
        movementSequence.add(currentFingerPosition)
        processMovementSequence(movementSequence)
        movementSequence.clear()
        currentLetter = null
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    fun movementCanceled() {
        longPressHandler.removeCallbacks(longPressRunnable)
        isLongPressCallbackSet = false
        movementSequence.clear()
        currentLetter = null
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    private fun initiateLongPressDetection() {
        isLongPressCallbackSet = true
        longPressHandler.postDelayed(longPressRunnable, DELAY_MILLIS_LONG_PRESS_INITIATION.toLong())
    }

    private fun interruptLongPress() {
        longPressHandler.removeCallbacks(longPressRunnable)
        processMovementSequence(movementSequence + FingerPosition.LONG_PRESS_END)
        isLongPressCallbackSet = false
    }

    private fun processMovementSequence(movementSequence: MovementSequence) {
        val actionMap = keyboardData.getOrNull()?.actionMap ?: return
        actionMap
            .getOrNone(movementSequence)
            .recover {
                actionMap
                    .getOrNone(
                        listOf(FingerPosition.NO_TOUCH) + movementSequence
                    )
                    .filter { currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT }
                    .bind()
            }
            .onSome {
                if (it.keyboardActionType == KeyboardActionType.INPUT_TEXT) {
                    handleInputText(it)
                } else {
                    handleInputKey(it.keyEventCode, it.keyFlags)
                }
            }
    }
}
