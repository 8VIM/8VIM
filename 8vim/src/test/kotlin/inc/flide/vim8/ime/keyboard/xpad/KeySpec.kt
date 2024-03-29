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
    lateinit var data: KeyboardData
    lateinit var key: Key
    val action = KeyboardAction.UNSPECIFIED.copy(text = "a", capsLockText = "A")

    beforeSpec {
        mockkStatic("inc.flide.vim8.ime.layout.models.KeyboardDataKt")
    }

    beforeTest {
        data = mockk(relaxed = true)
        key = Key(
            0,
            mockk(relaxed = true) {
                every { trailColor } returns Color.Black
                every { keyboardData } returns data
            }
        )
    }

    context("Getting text") {
        test("Invalid layer") {
            every { data.characterSets(any()) } returns None
            key.text(isCapitalize = false) shouldBe ""
        }

        test("Invalid index") {
            every { data.characterSets(any()) } returns emptyList<KeyboardAction?>().some()
            key.text(isCapitalize = false) shouldBe ""
        }

        test("No action") {
            every { data.characterSets(any()) } returns listOf<KeyboardAction?>(null).some()
            key.text(isCapitalize = false) shouldBe ""
        }

        test("Lowercase") {
            every {
                data.characterSets(any())
            } returns listOf<KeyboardAction?>(action).some()
            key.text(isCapitalize = false) shouldBe action.text
        }

        test("Uppercase") {
            every {
                data.characterSets(any())
            } returns listOf<KeyboardAction?>(action).some()
            key.text(isCapitalize = true) shouldBe action.capsLockText
        }
    }
})
