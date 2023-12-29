package inc.flide.vim8.ime.layout.models

import android.view.KeyEvent
import inc.flide.vim8.Vim8ImeService
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify

class CustomKeycodeSpec : FunSpec({
    val vim8ImeService = mockk<Vim8ImeService>(relaxed = true)

    context("handleKeyCode") {
        withData(
            nameFn = { it.first.name },
            (
                CustomKeycode.SELECTION_START to {
                    verify {
                        vim8ImeService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_LEFT to {
                    verify {
                        vim8ImeService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT to {
                    verify {
                        vim8ImeService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_UP to {
                    verify {
                        vim8ImeService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_UP,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.MOVE_CURRENT_END_POINT_DOWN to {
                    verify {
                        vim8ImeService.sendDownKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            0
                        )
                    }
                    verify {
                        vim8ImeService.sendUpKeyEvent(
                            KeyEvent.KEYCODE_SHIFT_LEFT,
                            0
                        )
                    }
                }
                ),
            (
                CustomKeycode.SELECT_ALL to {
                    verify {
                        vim8ImeService.sendDownAndUpKeyEvent(
                            KeyEvent.KEYCODE_A,
                            KeyEvent.META_CTRL_ON
                        )
                    }
                }
                ),
            (
                CustomKeycode.TOGGLE_SELECTION_ANCHOR to {
                    verify { vim8ImeService.switchAnchor() }
                }
                ),
            (
                CustomKeycode.SHIFT_TOGGLE to {
                    verify { vim8ImeService.performShiftToggle() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_MAIN_KEYPAD to {
                    verify { vim8ImeService.switchToMainKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_NUMBER_KEYPAD to {
                    verify { vim8ImeService.switchToNumberPad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD to {
                    verify { vim8ImeService.switchToSymbolsKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_SELECTION_KEYPAD to {
                    verify { vim8ImeService.switchToSelectionKeypad() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD to {
                    verify { vim8ImeService.switchToExternalEmoticonKeyboard() }
                }
                ),
            (
                CustomKeycode.HIDE_KEYBOARD to {
                    verify { vim8ImeService.hideKeyboard() }
                }
                ),
            (
                CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD to {
                    verify { vim8ImeService.switchToClipboardKeypad() }
                }
                )
        ) { (keycode, check) ->
            keycode.handleKeyCode(vim8ImeService)
            check()
        }
    }
    afterTest {
        clearMocks(vim8ImeService)
    }
})
