package inc.flide.vim8.keyboardActionListners

import android.os.Handler
import android.view.View
import inc.flide.vim8.keyboardHelpers.KeyboardDataStore
import inc.flide.vim8.structures.*
import java.util.*

class MainKeypadActionListener(view: View) : KeypadActionListener(view) {

    private val longPressHandler: Handler = Handler()
    private val movementSequence: MutableList<FingerPosition> = ArrayList()
    private var currentFingerPosition: FingerPosition = FingerPosition.NO_TOUCH
    private var isLongPressCallbackSet = false
    private var currentMovementSequenceType: MovementSequenceType = MovementSequenceType.NO_MOVEMENT
    private val longPressRunnable: Runnable = object : Runnable {
        override fun run() {
            val movementSequenceAugmented: MutableList<FingerPosition> = ArrayList(movementSequence)
            movementSequenceAugmented.add(FingerPosition.LONG_PRESS)
            processMovementSequence(movementSequenceAugmented)
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION.toLong())
        }
    }

    fun movementStarted(fingerPosition: FingerPosition) {
        currentFingerPosition = fingerPosition
        movementSequence.clear()
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
        movementSequence.add(currentFingerPosition)
        initiateLongPressDetection()
    }

    fun movementContinues(fingerPosition: FingerPosition) {
        val lastKnownFingerPosition = currentFingerPosition
        currentFingerPosition = fingerPosition
        val isFingerPositionChanged = lastKnownFingerPosition != currentFingerPosition
        if (isFingerPositionChanged) {
            interruptLongPress()
            movementSequence.add(currentFingerPosition)
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && KeyboardDataStore.keyboardData.getActionMap()[movementSequence] != null) {
                processMovementSequence(movementSequence)
                movementSequence.clear()
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT
                movementSequence.add(currentFingerPosition)
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
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    fun movementCanceled() {
        longPressHandler.removeCallbacks(longPressRunnable)
        isLongPressCallbackSet = false
        movementSequence.clear()
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    private fun initiateLongPressDetection() {
        isLongPressCallbackSet = true
        longPressHandler.postDelayed(longPressRunnable, Constants.DELAY_MILLIS_LONG_PRESS_INITIATION.toLong())
    }

    private fun interruptLongPress() {
        longPressHandler.removeCallbacks(longPressRunnable)
        val movementSequenceAugmented: MutableList<FingerPosition> = ArrayList(movementSequence)
        movementSequenceAugmented.add(FingerPosition.LONG_PRESS_END)
        processMovementSequence(movementSequenceAugmented)
        isLongPressCallbackSet = false
    }

    private fun processMovementSequence(movementSequence: MutableList<FingerPosition>) {
        var keyboardAction = KeyboardDataStore.keyboardData.getActionMap()[movementSequence]
        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            val modifiedMovementSequence: MutableList<FingerPosition> = ArrayList(movementSequence)
            modifiedMovementSequence.add(0, FingerPosition.NO_TOUCH)
            keyboardAction = KeyboardDataStore.keyboardData.getActionMap()[modifiedMovementSequence]
        }
        if (keyboardAction == null) {
            movementSequence.clear()
            return
        }
        when (keyboardAction.getKeyboardActionType()) {
            KeyboardActionType.INPUT_TEXT -> handleInputText(keyboardAction)
            KeyboardActionType.INPUT_KEY -> handleInputKey(keyboardAction)
        }
    }

}