package inc.flide.vim8.ime.keyboard.text

import android.view.KeyEvent
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.input.KeyVariation
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class KeyboardState protected constructor(open var rawValue: ULong) {
    companion object {
        const val M_IME_UI_MODE: ULong = 0x0Fu
        const val O_IME_UI_MODE: Int = 0
        const val M_KEY_VARIATION: ULong = 0x0Fu
        const val O_KEY_VARIATION: Int = 4
        const val M_INPUT_SHIFT_STATE: ULong = 0x03u
        const val O_INPUT_SHIFT_STATE: Int = 8

        const val F_IS_CTRL_ON: ULong = 0x00000400u
        const val F_IS_FN_ON: ULong = 0x00000800u

        const val STATE_ALL_ZERO: ULong = 0uL

        fun new(value: ULong = STATE_ALL_ZERO) = KeyboardState(value)
    }

    fun snapshot(): KeyboardState {
        return new(rawValue)
    }

    private fun getFlag(f: ULong): Boolean {
        return (rawValue and f) != STATE_ALL_ZERO
    }

    private fun setFlag(f: ULong, v: Boolean) {
        rawValue = if (v) {
            rawValue or f
        } else {
            rawValue and f.inv()
        }
    }

    private fun getRegion(m: ULong, o: Int): Int {
        return ((rawValue shr o) and m).toInt()
    }

    private fun setRegion(m: ULong, o: Int, v: Int) {
        rawValue = (rawValue and (m shl o).inv()) or ((v.toULong() and m) shl o)
    }

    var keyVariation: KeyVariation
        get() = KeyVariation.fromInt(getRegion(M_KEY_VARIATION, O_KEY_VARIATION))
        set(v) {
            setRegion(M_KEY_VARIATION, O_KEY_VARIATION, v.toInt())
        }

    var imeUiMode: ImeUiMode
        get() = ImeUiMode.fromInt(getRegion(M_IME_UI_MODE, O_IME_UI_MODE))
        set(v) {
            setRegion(M_IME_UI_MODE, O_IME_UI_MODE, v.toInt())
        }

    var inputShiftState: InputShiftState
        get() = InputShiftState.fromInt(getRegion(M_INPUT_SHIFT_STATE, O_INPUT_SHIFT_STATE))
        set(v) {
            setRegion(M_INPUT_SHIFT_STATE, O_INPUT_SHIFT_STATE, v.toInt())
        }

    val isUppercase: Boolean
        get() = inputShiftState != InputShiftState.UNSHIFTED

    var isCtrlOn: Boolean
        get() = getFlag(F_IS_CTRL_ON)
        set(v) {
            setFlag(F_IS_CTRL_ON, v)
        }

    var isFnOn: Boolean
        get() = getFlag(F_IS_FN_ON)
        set(v) {
            setFlag(F_IS_FN_ON, v)
        }

    val ctrlFlag: Int
        get() = if (isCtrlOn) KeyEvent.META_CTRL_MASK else 0

    val shiftFlag: Int
        get() = if (isUppercase) KeyEvent.META_SHIFT_MASK else 0
}

class ObservableKeyboardState private constructor(
    initValue: ULong,
    private val dispatchFlow: MutableStateFlow<KeyboardState> = MutableStateFlow(
        KeyboardState.new(
            initValue
        )
    )
) : KeyboardState(initValue), StateFlow<KeyboardState> by dispatchFlow {
    companion object {
        const val BATCH_ZERO: Int = 0

        fun new(value: ULong = STATE_ALL_ZERO) = ObservableKeyboardState(value)
    }

    override var rawValue by Delegates
        .observable(initValue) { _, old, new ->
            if (old != new) dispatchState()
        }
    private val batchEditCount = AtomicInteger(BATCH_ZERO)

    init {
        dispatchState()
    }

    private fun dispatchState() {
        if (batchEditCount.get() == BATCH_ZERO) {
            dispatchFlow.value = this.snapshot()
        }
    }

    fun beginBatchEdit() {
        batchEditCount.incrementAndGet()
    }

    /**
     * Ends a batch edit. Will dispatch the current state if there are no more other batch edits active. This method is
     * thread-safe and can be called from any thread.
     */
    fun endBatchEdit() {
        batchEditCount.decrementAndGet()
        dispatchState()
    }

    /**
     * Performs a batch edit by executing the modifier [block]. Any exception that [block] throws will be caught and
     * re-thrown after correctly ending the batch edit.
     */
    inline fun batchEdit(block: (ObservableKeyboardState) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        beginBatchEdit()
        try {
            block(this)
        } catch (e: Throwable) {
            throw e
        } finally {
            endBatchEdit()
        }
    }
}
