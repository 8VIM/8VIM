package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Size
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SizeExtensionSpec : FunSpec({
    val screenSize = Size(10f, 10f)

    context("min/max width/height") {
        test("min") {
            screenSize.minWidth shouldBe 6f
            screenSize.minHeight shouldBe 6f
        }

        test("max") {
            screenSize.maxWidth shouldBe 8f
            screenSize.maxHeight shouldBe 8f
        }
    }

    test("coerceIn") {
        Size.Zero.coerceIn(screenSize) shouldBe Size(6f, 6f)
    }
})
