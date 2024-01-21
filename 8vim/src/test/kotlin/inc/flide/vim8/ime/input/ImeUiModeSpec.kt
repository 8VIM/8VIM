package inc.flide.vim8.ime.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class ImeUiModeSpec : FunSpec({
    context("fromInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (-1 to ImeUiMode.TEXT),
            (0 to ImeUiMode.TEXT),
            (1 to ImeUiMode.SYMBOLS),
            (2 to ImeUiMode.NUMERIC),
            (3 to ImeUiMode.CLIPBOARD),
            (4 to ImeUiMode.SELECTION)
        ) { (int, state) ->
            ImeUiMode.fromInt(int) shouldBe state
        }
    }

    context("toInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (0 to ImeUiMode.TEXT),
            (1 to ImeUiMode.SYMBOLS),
            (2 to ImeUiMode.NUMERIC),
            (3 to ImeUiMode.CLIPBOARD),
            (4 to ImeUiMode.SELECTION)
        ) { (int, state) ->
            state.toInt() shouldBe int
        }
    }
})
