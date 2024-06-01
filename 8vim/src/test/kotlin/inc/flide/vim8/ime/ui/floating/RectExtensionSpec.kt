package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import inc.flide.vim8.lib.android.offset
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec

class RectExtensionSpec : FunSpec({
    val screenSize = Size(10f, 10f)

    test("empty rect") {
        Rect.Zero.boundRectIntoScreen(screenSize).shouldBeNone()
    }

    test("not resizing") {
        val rect = Rect(Offset(0f, -7f), Size(6f, 6f))
        rect.boundRectIntoScreen(screenSize) shouldBeSome rect
    }

    context("resizing") {
        test("size") {
            val rect = Rect(Offset(0f, -9f), Size(9f, 9f))
            rect.boundRectIntoScreen(screenSize) shouldBeSome Rect(rect.offset, Size(8f, 8f))
        }

        test("offset") {
            val offset = Offset(0f, -7f)
            val rect = Rect(offset, Size(8f, 8f))
            rect.boundRectIntoScreen(screenSize) shouldBeSome Rect(
                offset.copy(y = rect.size.height * -0.9f),
                rect.size
            )
        }
    }
})
