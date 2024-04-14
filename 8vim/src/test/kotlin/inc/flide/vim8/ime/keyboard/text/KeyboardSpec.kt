package inc.flide.vim8.ime.keyboard.text

import inc.flide.vim8.ime.layout.models.KeyboardAction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KeyboardSpec : FunSpec({
    val desiredKey = Key(action = KeyboardAction.UNSPECIFIED).also {
        it.touchBounds.apply {
            width = 5f
            height = 5f
        }
        it.visibleBounds.applyFrom(it.touchBounds).deflateBy(1f, 1f)
    }
    val keys = (1..4)
        .map { Key(action = it.toKeyboardAction()) }
        .windowed(2, 2)
        .map { it.toTypedArray() }
        .toTypedArray()

    test("Layout") {
        val keyboard = Keyboard(keys)
        keyboard.layout(10f, 10f, desiredKey)
        keyboard.getKeyForPos(2.5f, 2.5f)?.action?.keyEventCode shouldBe 1
        keyboard.getKeyForPos(11f, 2.5f) shouldBe null
    }
})
