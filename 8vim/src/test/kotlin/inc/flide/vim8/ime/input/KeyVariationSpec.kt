package inc.flide.vim8.ime.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class KeyVariationSpec : FunSpec({
    context("fromInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (-1 to KeyVariation.ALL),
            (0 to KeyVariation.ALL),
            (1 to KeyVariation.NORMAL),
            (2 to KeyVariation.PASSWORD)
        ) { (int, state) ->
            KeyVariation.fromInt(int) shouldBe state
        }
    }

    context("toInt") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (0 to KeyVariation.ALL),
            (1 to KeyVariation.NORMAL),
            (2 to KeyVariation.PASSWORD)
        ) { (int, state) ->
            state.toInt() shouldBe int
        }
    }
})
