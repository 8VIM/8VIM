package inc.flide.vim8.ime.keyboard.text

import android.content.Context
import android.view.KeyEvent
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.editorInstance
import inc.flide.vim8.ime.editor.ImeOptions
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.InputEventDispatcher
import inc.flide.vim8.ime.input.InputKeyEventReceiver
import inc.flide.vim8.ime.input.InputShiftState
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType

class KeyboardManager(context: Context) : InputKeyEventReceiver {
    private val prefs by appPreferenceModel()
    private val editorInstance by context.editorInstance()
    val activeState = ObservableKeyboardState.new()

    private val repeatableKeyCodes = intArrayOf(
        CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.keyCode,
        CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT.keyCode,
        CustomKeycode.MOVE_CURRENT_END_POINT_UP.keyCode,
        CustomKeycode.MOVE_CURRENT_END_POINT_DOWN.keyCode,
    )
    private val repeatableKeyCodesSet = repeatableKeyCodes.toSet()

    val inputEventDispatcher = InputEventDispatcher
        .new(repeatableKeyCodes = repeatableKeyCodes)
        .also { it.keyEventReceiver = this }

    init {
        val moveByWord = prefs.keyboard.behavior.cursor.moveByWord
        activeState.isCtrlOn = moveByWord.get()

        moveByWord.observe {
            if (it != activeState.isCtrlOn) {
                activeState.isCtrlOn = it
            }
        }
    }

    private fun getFlags(keyCode: Int, keyFlags: Int): Int = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT -> keyFlags or activeState.ctrlFlag

        else -> keyFlags
    }

    private fun handleKeyCode(keyCode: Int, keyFlags: Int = 0) {
        val customKeycode = CustomKeycode.KEY_CODE_TO_STRING_CODE_MAP[keyCode]
        if (customKeycode != null) {
            handleKeyCode(customKeycode)
        } else {
            when (keyCode) {
                KeyEvent.KEYCODE_CUT -> editorInstance.performCut()
                KeyEvent.KEYCODE_COPY -> editorInstance.performCopy()
                KeyEvent.KEYCODE_PASTE -> editorInstance.performPaste()
                KeyEvent.KEYCODE_ENTER -> handleEnter()
                KeyEvent.KEYCODE_DEL -> editorInstance.performDelete()
                else -> {
                    val flags = getFlags(keyCode, keyFlags)
                    editorInstance.sendDownAndUpKeyEvent(keyCode, flags)
                    activeState.inputShiftState = InputShiftState.UNSHIFTED
                }
            }
        }
        Vim8ImeService.inputFeebackController()?.keyPress(keyCode)
    }

    private fun handleKeyCode(keycode: CustomKeycode) {
        when (keycode) {
            CustomKeycode.SWITCH_TO_MAIN_KEYPAD -> activeState.batchEdit {
                it.imeUiMode = ImeUiMode.TEXT
            }

            CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD -> Vim8ImeService.switchToEmoticonKeyboard()
            CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD -> activeState.batchEdit {
                it.imeUiMode = ImeUiMode.CLIPBOARD
            }

            CustomKeycode.SWITCH_TO_SELECTION_KEYPAD -> activeState.batchEdit {
                it.imeUiMode = ImeUiMode.SELECTION
            }

            CustomKeycode.TOGGLE_SELECTION_ANCHOR -> editorInstance.performSwitchAnchor()
            CustomKeycode.CTRL_TOGGLE -> handleCtrl()
            CustomKeycode.SHIFT_TOGGLE -> handleShift()
            CustomKeycode.MOVE_CURRENT_END_POINT_LEFT,
            CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT,
            CustomKeycode.MOVE_CURRENT_END_POINT_UP,
            CustomKeycode.MOVE_CURRENT_END_POINT_DOWN -> handleMove(keycode)

            CustomKeycode.SELECTION_START -> handleSelectionStart()
            CustomKeycode.SELECT_ALL -> handleSelectAll()
            CustomKeycode.HIDE_KEYBOARD -> Vim8ImeService.hideKeyboard()
            else -> {}
        }
    }

    private fun handleText(keyboardAction: KeyboardAction) {
        val text =
            if (activeState.isUppercase && keyboardAction.capsLockText.isNotEmpty())
                keyboardAction.capsLockText
            else keyboardAction.text
        editorInstance.commitText(text)
        activeState.inputShiftState = InputShiftState.UNSHIFTED
        Vim8ImeService.inputFeebackController()?.keyPress()
    }

    private fun handleShift() {
        activeState.inputShiftState = when (activeState.inputShiftState) {
            InputShiftState.UNSHIFTED -> InputShiftState.SHIFTED
            InputShiftState.SHIFTED -> InputShiftState.CAPS_LOCK
            InputShiftState.CAPS_LOCK -> InputShiftState.UNSHIFTED
        }
    }

    private fun handleMove(keycode: CustomKeycode) {
        if (activeState.inputShiftState != InputShiftState.UNSHIFTED) {
            editorInstance.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }

        editorInstance.sendDownAndUpKeyEvent(keycode.toKeyEvent(), activeState.ctrlFlag)

        if (activeState.inputShiftState != InputShiftState.UNSHIFTED) {
            editorInstance.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        }
    }

    private fun handleSelectionStart() {
        editorInstance.sendDownKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        editorInstance.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, activeState.ctrlFlag)
        editorInstance.sendUpKeyEvent(KeyEvent.KEYCODE_SHIFT_LEFT, 0)
    }

    private fun handleSelectAll() {
        editorInstance.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)
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

    override fun onInputKeyDown(keyboardAction: KeyboardAction) {
        if (keyboardAction.keyboardActionType == KeyboardActionType.INPUT_KEY &&
            repeatableKeyCodesSet.contains(keyboardAction.keyEventCode)
        ) {
            handleKeyCode(keyboardAction.keyEventCode, keyboardAction.keyFlags)
        }
    }

    override fun onInputKeyUp(keyboardAction: KeyboardAction) {
        when (keyboardAction.keyboardActionType) {
            KeyboardActionType.INPUT_TEXT -> handleText(keyboardAction)
            KeyboardActionType.INPUT_KEY ->
                if (!repeatableKeyCodesSet.contains(keyboardAction.keyEventCode)) {
                    handleKeyCode(keyboardAction.keyEventCode, keyboardAction.keyFlags)
                }
        }
    }

    override fun onInputKeyRepeat(keyboardAction: KeyboardAction) {
        if (keyboardAction.keyboardActionType == KeyboardActionType.INPUT_KEY &&
            repeatableKeyCodesSet.contains(keyboardAction.keyEventCode)
        ) {
            handleKeyCode(keyboardAction.keyEventCode, keyboardAction.keyFlags)
        }
    }
}
