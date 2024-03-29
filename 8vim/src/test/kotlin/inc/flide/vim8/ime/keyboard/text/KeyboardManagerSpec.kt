package inc.flide.vim8.ime.keyboard.text

import android.content.Context
import android.view.KeyEvent
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceObserver
import inc.flide.vim8.editorInstance
import inc.flide.vim8.ime.editor.EditorInstance
import inc.flide.vim8.ime.editor.ImeOptions
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.InputEventDispatcher
import inc.flide.vim8.ime.input.InputFeedbackController
import inc.flide.vim8.ime.input.InputKeyEventReceiver
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.layout.models.CustomKeycode
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.Runs
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder

class KeyboardManagerSpec : FunSpec(
    {
        lateinit var context: Context
        lateinit var keyboardState: ObservableKeyboardState
        lateinit var editor: EditorInstance
        lateinit var observer: PreferenceObserver<Boolean>
        val inputFeedbackController = mockk<InputFeedbackController>(relaxed = true)
        val repeatableKeyCodes = intArrayOf(
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.keyCode,
            CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT.keyCode,
            CustomKeycode.MOVE_CURRENT_END_POINT_UP.keyCode,
            CustomKeycode.MOVE_CURRENT_END_POINT_DOWN.keyCode
        ).toSet()

        fun verifyKeyCode(keyCode: Int) {
            when (keyCode) {
                KeyEvent.KEYCODE_CUT -> verify { editor.performCut() }
                KeyEvent.KEYCODE_COPY -> verify { editor.performCopy() }
                KeyEvent.KEYCODE_PASTE -> verify { editor.performPaste() }
                KeyEvent.KEYCODE_DEL -> verify { editor.performDelete() }
                KeyEvent.KEYCODE_ENTER -> verify { editor.performEnter() }
                CustomKeycode.SWITCH_TO_MAIN_KEYPAD.keyCode ->
                    verify {
                        keyboardState setProperty "imeUiMode" value ImeUiMode.TEXT
                    }

                CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD.keyCode ->
                    verify {
                        keyboardState setProperty "imeUiMode" value ImeUiMode.CLIPBOARD
                    }

                CustomKeycode.SWITCH_TO_SYMBOLS_KEYPAD.keyCode ->
                    verify {
                        keyboardState setProperty "imeUiMode" value ImeUiMode.SYMBOLS
                    }

                CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.keyCode ->
                    verify {
                        keyboardState setProperty "imeUiMode" value ImeUiMode.SELECTION
                    }

                CustomKeycode.SWITCH_TO_NUMBER_KEYPAD.keyCode ->
                    verify {
                        keyboardState setProperty "imeUiMode" value ImeUiMode.NUMERIC
                    }

                CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.keyCode ->
                    verify { Vim8ImeService.switchToEmoticonKeyboard() }

                CustomKeycode.TOGGLE_SELECTION_ANCHOR.keyCode ->
                    verify { editor.performSwitchAnchor() }

                CustomKeycode.CTRL_TOGGLE.keyCode ->
                    verify { keyboardState setProperty "isCtrlOn" value true }

                CustomKeycode.SHIFT_TOGGLE.keyCode ->
                    verify {
                        keyboardState setProperty "inputShiftState" value InputShiftState.UNSHIFTED
                    }

                CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.keyCode,
                CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT.keyCode,
                CustomKeycode.MOVE_CURRENT_END_POINT_UP.keyCode,
                CustomKeycode.MOVE_CURRENT_END_POINT_DOWN.keyCode -> verify {
                    editor.sendDownAndUpKeyEvent(
                        CustomKeycode.KEY_CODE_TO_STRING_CODE_MAP[keyCode]!!.toKeyEvent(),
                        any()
                    )
                }

                CustomKeycode.NO_OPERATION.keyCode -> {}
                CustomKeycode.SELECTION_START.keyCode -> verify {
                    editor.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, any())
                }

                CustomKeycode.SELECT_ALL.keyCode -> verify {
                    editor.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
                }

                CustomKeycode.HIDE_KEYBOARD.keyCode -> verify { Vim8ImeService.hideKeyboard() }

                else -> verify {
                    editor.sendDownAndUpKeyEvent(keyCode, any())
                }
            }
        }
        beforeSpec {
            mockkStatic(Context::editorInstance)
            mockkStatic(::appPreferenceModel)
            mockkObject(ObservableKeyboardState)
            mockkObject(Vim8ImeService)
            mockkConstructor(InputEventDispatcher::class)

            context = mockk {
                every { editorInstance() } answers { lazy { editor } }
            }

            every { ObservableKeyboardState.new() } answers { keyboardState }
            every { appPreferenceModel() } returns CachedPreferenceModel(
                mockk {
                    every { keyboard } returns mockk {
                        every { behavior } returns mockk {
                            every { cursor } returns mockk {
                                every { moveByWord } returns mockk(relaxed = true) {
                                    every { get() } returns false
                                    every { observe(any()) } answers { observer = firstArg() }
                                }
                            }
                        }
                    }
                }
            )

            every { Vim8ImeService.inputFeedbackController() } returns inputFeedbackController
            every { Vim8ImeService.switchToEmoticonKeyboard() } just Runs
            every { Vim8ImeService.hideKeyboard() } just Runs

            every {
                anyConstructed<InputEventDispatcher>().keyEventReceiver = any()
            } propertyType InputKeyEventReceiver::class answers { value }
        }

        beforeTest {
            editor = mockk(relaxed = true) {
                every { imeOptions } returns mockk(relaxed = true) {
                    every { action } returns ImeOptions.Action.DONE
                    every { flagNoEnterAction } returns true
                }
            }
            keyboardState = mockk(relaxed = true) {
                every { isCtrlOn } returns false
                every { inputShiftState } returns InputShiftState.CAPS_LOCK
            }
        }

        test("Observe ctrl on switch") {
            KeyboardManager(context)
            observer.onChanged(true)
            verifyOrder {
                keyboardState setProperty "isCtrlOn" value false
                keyboardState setProperty "isCtrlOn" value true
            }
        }

        context("onInputKeyDown text") {
            withData(nameFn = { "Shift: $it" }, listOf(true, false)) { isShift ->
                withData(nameFn = { "Uppercase: $it" }, listOf(true, false)) { isUppercase ->
                    withData(
                        nameFn = { "Action: ${it.text}, ${it.capsLockText}" },
                        "a".toKeyboardAction(),
                        "a".toKeyboardAction().copy(capsLockText = "A")
                    ) {
                        every { keyboardState.isUppercase } returns isUppercase
                        every { keyboardState.inputShiftState } returns if (isShift) {
                            InputShiftState.SHIFTED
                        } else {
                            InputShiftState.UNSHIFTED
                        }
                        val manager = KeyboardManager(context)
                        manager.onInputKeyDown(it, false)
                        val text = if (isUppercase && it.capsLockText.isNotEmpty()) {
                            it.capsLockText
                        } else {
                            it.text
                        }
                        verifyOrder {
                            inputFeedbackController.keyPress(any(), any())
                            editor.commitText(text)
                            if (isShift) {
                                keyboardState
                                    .setProperty("inputShiftState")
                                    .value(InputShiftState.UNSHIFTED)
                            }
                        }
                    }
                }
            }
        }

        context("onInputKeyDown repeatable key") {
            withData(
                nameFn = { "Action: ${it.keyEventCode}" },
                repeatableKeyCodes.map { it.toKeyboardAction() }
            ) {
                val manager = KeyboardManager(context)
                manager.onInputKeyDown(it, false)
                verify {
                    inputFeedbackController.keyPress(any(), any())
                }
                verifyKeyCode(it.keyEventCode)
            }
        }

        context("onInputKeyUp") {
            withData(
                nameFn = { "Action: ${it.keyEventCode}" },
                (
                    (
                        CustomKeycode.KEY_CODE_TO_STRING_CODE_MAP.keys + setOf(
                            KeyEvent.KEYCODE_CUT,
                            KeyEvent.KEYCODE_COPY,
                            KeyEvent.KEYCODE_PASTE,
                            KeyEvent.KEYCODE_ENTER
                        )
                        ) - repeatableKeyCodes
                    ).map { it.toKeyboardAction() }
            ) {
                val manager = KeyboardManager(context)
                manager.onInputKeyUp(it, false)
                verify {
                    inputFeedbackController.keyPress(any(), any())
                }
                verifyKeyCode(it.keyEventCode)
            }
        }
        afterSpec {
            clearStaticMockk(Context::class)
            clearConstructorMockk(InputEventDispatcher::class)
            unmockkObject(ObservableKeyboardState, Vim8ImeService)
        }
    }
)
