package inc.flide.vim8.ime.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class InputShiftStateSpec : FunSpec({
    context("fromInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (-1 to InputShiftState.UNSHIFTED),
            (0 to InputShiftState.UNSHIFTED),
            (1 to InputShiftState.SHIFTED),
            (2 to InputShiftState.CAPS_LOCK)
        ) { (int, state) ->
            InputShiftState.fromInt(int) shouldBe state
        }
    }

    context("toInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (0 to InputShiftState.UNSHIFTED),
            (1 to InputShiftState.SHIFTED),
            (2 to InputShiftState.CAPS_LOCK)
        ) { (int, state) ->
            state.toInt() shouldBe int
        }
    }
})
