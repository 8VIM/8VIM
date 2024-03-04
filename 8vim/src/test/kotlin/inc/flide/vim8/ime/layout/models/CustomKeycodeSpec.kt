package inc.flide.vim8.ime.layout.models

import android.view.KeyEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class CustomKeycodeSpec : FunSpec({
    context("convert to a keyEvent") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            (CustomKeycode.MOVE_CURRENT_END_POINT_LEFT to KeyEvent.KEYCODE_DPAD_LEFT),
            (CustomKeycode.SELECTION_START to KeyEvent.KEYCODE_DPAD_LEFT),
            (CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT to KeyEvent.KEYCODE_DPAD_RIGHT),
            (CustomKeycode.MOVE_CURRENT_END_POINT_UP to KeyEvent.KEYCODE_DPAD_UP),
            (CustomKeycode.MOVE_CURRENT_END_POINT_DOWN to KeyEvent.KEYCODE_DPAD_DOWN),
            (CustomKeycode.SELECT_ALL to 0)
        ) { (customKeycode, keyEventCode) ->
            customKeycode.toKeyEvent() shouldBe keyEventCode
        }
    }
})
