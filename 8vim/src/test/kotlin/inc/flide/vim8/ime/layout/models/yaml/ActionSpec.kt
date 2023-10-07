package inc.flide.vim8.ime.layout.models.yaml

import android.view.KeyEvent
import inc.flide.vim8.ime.layout.models.CustomKeycode
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic

class ActionSpec : FunSpec({
    beforeSpec {
        mockkStatic(KeyEvent::keyCodeFromString)
        every { KeyEvent.keyCodeFromString(any()) } returns KeyEvent.KEYCODE_UNKNOWN
        every { KeyEvent.keyCodeFromString("KEYCODE_A") } returns KeyEvent.KEYCODE_A
    }

    context("checking an action") {
        test("is empty") {
            Action().isEmpty().shouldBeTrue()
        }
        test("is not empty") {
            Action(lowerCase = "test").isEmpty().shouldBeFalse()
        }
    }

    context("Getting keyCode value from string") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            ("KEYCODE_A" to KeyEvent.KEYCODE_A),
            ("MOVE_CURRENT_END_POINT_LEFT" to CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.keyCode),
            ("NOT_VALID" to 0)
        ) { (keyCodeString, value) ->
            Action(keyCodeString = keyCodeString).keyCode() shouldBe value
        }
    }
})
