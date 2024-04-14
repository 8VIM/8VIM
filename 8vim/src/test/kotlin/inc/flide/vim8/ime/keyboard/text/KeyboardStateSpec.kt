package inc.flide.vim8.ime.keyboard.text

import android.view.KeyEvent
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.input.KeyVariation
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.enum

class KeyboardStateSpec : FunSpec({
    context("KeyVariation") {
        checkAll(Exhaustive.enum<KeyVariation>()) {
            val state = KeyboardState.new()
            state.keyVariation shouldBe KeyVariation.ALL
            state.keyVariation = it
            state.keyVariation shouldBe it
        }
    }

    context("ImeUiMode") {
        checkAll(Exhaustive.enum<ImeUiMode>()) {
            val state = KeyboardState.new()
            state.imeUiMode shouldBe ImeUiMode.TEXT
            state.imeUiMode = it
            state.imeUiMode shouldBe it
        }
    }

    context("InputShiftState") {
        checkAll(Exhaustive.enum<InputShiftState>()) {
            val state = KeyboardState.new()
            state.inputShiftState shouldBe InputShiftState.UNSHIFTED
            state.inputShiftState = it
            state.inputShiftState shouldBe it
            val isUppercase = it != InputShiftState.UNSHIFTED
            state.isUppercase shouldBe isUppercase
            state.shiftFlag shouldBe (if (isUppercase) KeyEvent.META_SHIFT_MASK else 0)
        }
    }

    context("CtrlOn") {
        checkAll(Exhaustive.boolean()) {
            val state = KeyboardState.new()
            state.isCtrlOn.shouldBeFalse()
            state.isCtrlOn = it
            state.isCtrlOn shouldBe it
            state.ctrlFlag shouldBe if (it) KeyEvent.META_CTRL_MASK else 0
        }
    }
})
