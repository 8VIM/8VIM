package inc.flide.vim8.ime.input

import inc.flide.vim8.arbitraries.Arbitraries
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.delay

class InputEventDispatcherSpec : FunSpec({
    val receiver = mockk<InputKeyEventReceiver>(relaxed = true)
    val action = Arbitraries.arbKeyboardAction.next()
    beforeTest {
        clearMocks(receiver)
    }

    context("sendDown and sendUp") {
        withData(nameFn = { "Repeat: $it" }, listOf(0, 2)) { repeat ->
            val dispatcher = InputEventDispatcher().also { it.keyEventReceiver = receiver }
            dispatcher.sendDown(action)
            delay(
                InputEventDispatcher.KeyRepeatTimeout + repeat * InputEventDispatcher.KeyRepeatDelay
            )
            dispatcher.sendUp(action)
            verifyOrder {
                receiver.onInputKeyDown(action, false)
                for (i in 0 until repeat) {
                    receiver.onInputKeyDown(action, true)
                }
                receiver.onInputKeyUp(action, false)
            }
        }
    }

    test("sendDownUp") {
        val dispatcher = InputEventDispatcher().also { it.keyEventReceiver = receiver }
        dispatcher.sendDownUp(action, repeat = false)
        receiver.onInputKeyDown(action, false)
        receiver.onInputKeyUp(action, false)
    }
})
