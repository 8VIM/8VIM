package inc.flide.vim8.ime.keyboard

import android.content.Context
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.editorInstance
import inc.flide.vim8.ime.editor.ImeOptions
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType

class KeyboardManager(context: Context) {
    private val prefs by appPreferenceModel()
    private val editorInstance by context.editorInstance()
    val activeState = ObservableKeyboardState.new()

    init {
        val moveByWord = prefs.keyboard.behavior.cursor.moveByWord
        activeState.isCtrlOn = moveByWord.get()
        moveByWord.observe {
            if (it != activeState.isCtrlOn) {
                activeState.isCtrlOn = it
            }
        }
    }

    fun handleInput(keycode: CustomKeycode) {
        when (keycode) {
            CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD -> Vim8ImeService.switchToEmoticonKeyboard()
            else -> {}
        }
    }

    fun handleInput(keyboardAction: KeyboardAction) {
        if (keyboardAction.keyboardActionType == KeyboardActionType.INPUT_TEXT) {
            val text =
                if (activeState.isUppercase && keyboardAction.capsLockText.isNotEmpty())
                    keyboardAction.capsLockText
                else keyboardAction.text
            handleText(text)
        }
    }

    private fun handleText(text: String) {
        editorInstance.commitText(text)
        activeState.inputShiftState = InputShiftState.UNSHIFTED
    }

    private fun handleShift() {
        activeState.inputShiftState = when (activeState.inputShiftState) {
            InputShiftState.UNSHIFTED -> InputShiftState.SHIFTED
            InputShiftState.SHIFTED -> InputShiftState.CAPS_LOCK
            InputShiftState.CAPS_LOCK -> InputShiftState.UNSHIFTED
        }
    }

    private fun handleCtrl() {
        activeState.isCtrlOn = !activeState.isCtrlOn
    }

    private fun handleEnter() {
        if (editorInstance.imeOptions.flagNoEnterAction) {
            editorInstance.performEnter()
        } else {
            when (val action = editorInstance.imeOptions.action) {
                ImeOptions.Action.DONE,
                ImeOptions.Action.GO,
                ImeOptions.Action.NEXT,
                ImeOptions.Action.PREVIOUS,
                ImeOptions.Action.SEARCH,
                ImeOptions.Action.SEND -> {
                    editorInstance.performEnterAction(action)
                }

                else -> editorInstance.performEnter()
            }
        }
    }
}
