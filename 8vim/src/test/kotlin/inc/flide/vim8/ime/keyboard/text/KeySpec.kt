package inc.flide.vim8.ime.keyboard.text

import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.LayerLevel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class KeySpec : FunSpec({
    context("String to KeyboardAction") {
        checkAll(Arb.string(2)) {
            it.toKeyboardAction() shouldBe KeyboardAction(
                keyboardActionType = KeyboardActionType.INPUT_TEXT,
                text = it,
                keyEventCode = 0,
                keyFlags = 0,
                layer = LayerLevel.FIRST
            )
        }
    }
    context("CustomKeyCode to KeyboardAction") {
        checkAll(Arb.enum<CustomKeycode>()) {
            it.toKeyboardAction() shouldBe KeyboardAction(
                keyboardActionType = KeyboardActionType.INPUT_KEY,
                text = "",
                keyEventCode = it.keyCode,
                keyFlags = 0,
                layer = LayerLevel.FIRST
            )
        }
    }
})
