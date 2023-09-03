package inc.flide.vim8.models

import android.view.KeyEvent
import inc.flide.vim8.MainInputMethodService
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify

class CustomKeycodeSpec : FunSpec({
    val mainInputMethodService = mockk<MainInputMethodService>(relaxed = true)

    context("handleKeyCode") {
        withData(
            nameFn = { it.first.name },
            (
                CustomKeycode.SELECTION_START to {
                    verify {
                        mainInputMethodService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_LEFT to {
                    verify {
                        mainInputMethodService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT to {
                    verify {
                        mainInputMethodService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_UP to {
                    verify {
                        mainInputMethodService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_UP,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_DOWN to {
                    verify {
                        mainInputMethodService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            0
                        )
                    }
                    verify {
                        mainInputMethodService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.SELECT_ALL to {
                    verify {
                        mainInputMethodService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_A,
                            KeyEvent.META_CTRL_ON
                        )
                    }
                }
                ),
            (
                CustomKeycode.TOGGLE_SELECTION_ANCHOR to {
                    verify { mainInputMethodService.switchAnchor() }
                }
                ),
            (
                CustomKeycode.SHIFT_TOGGLE to {
                    verify { mainInputMethodService.performShiftToggle() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_MAIN_KEYPAD to {
                    verify { mainInputMethodService.switchToMainKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_NUMBER_KEYPAD to {
                    verify { mainInputMethodService.switchToNumberPad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD to {
                    verify { mainInputMethodService.switchToSymbolsKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_SELECTION_KEYPAD to {
                    verify { mainInputMethodService.switchToSelectionKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD to {
                    verify { mainInputMethodService.switchToExternalEmoticonKeyboard() }
                }
                ),
            (
                CustomKeycode.HIDE_KEYBOARD to {
                    verify { mainInputMethodService.hideKeyboard() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD to {
                    verify { mainInputMethodService.switchToClipboardKeypad() }
                }
                )
        ) { (keycode, check) ->
            keycode.handleKeyCode(mainInputMethodService)
            check()
        }
    }
    afterTest {
        clearMocks(mainInputMethodService)
    }
})
