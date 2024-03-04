package inc.flide.vim8.ime.keyboard.xpad

import androidx.compose.ui.graphics.Color
import arrow.core.None
import arrow.core.some
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.characterSets
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class KeySpec : FunSpec({
    val keyboard = mockk<Keyboard>(relaxed = true)
    val keyboardData = mockk<KeyboardData>()
    val action = KeyboardAction.UNSPECIFIED.copy(text = "a", capsLockText = "A")
    val key = Key(0, keyboard)

    beforeSpec {
        every { keyboard.trailColor } returns Color.Black
        every { keyboard.keyboardData } returns keyboardData
        mockkStatic("inc.flide.vim8.ime.layout.models.KeyboardDataKt")
    }

    context("Getting text") {
        test("Invalid layer") {
            every { keyboardData.characterSets(any()) } returns None
            key.text(isCapitalize = false) shouldBe ""
        }

        test("Invalid index") {
            every { keyboardData.characterSets(any()) } returns emptyList<KeyboardAction?>().some()
            key.text(isCapitalize = false) shouldBe ""
        }

        test("No action") {
            every { keyboardData.characterSets(any()) } returns listOf<KeyboardAction?>(null).some()
            key.text(isCapitalize = false) shouldBe ""
        }

        test("Lowercase") {
            every {
                keyboardData.characterSets(any())
            } returns listOf<KeyboardAction?>(action).some()
            key.text(isCapitalize = false) shouldBe action.text
        }

        test("Uppercase") {
            every {
                keyboardData.characterSets(any())
            } returns listOf<KeyboardAction?>(action).some()
            key.text(isCapitalize = true) shouldBe action.capsLockText
        }
    }
})
