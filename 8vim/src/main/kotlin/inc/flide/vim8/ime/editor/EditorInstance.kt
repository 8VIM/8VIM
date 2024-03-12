package inc.flide.vim8.ime.editor

import android.content.Context
import android.os.SystemClock
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.KeyVariation
import inc.flide.vim8.ime.nlp.BreakIteratorGroup
import inc.flide.vim8.keyboardManager

class EditorInstance(context: Context) {
    private val keyboardManager by context.keyboardManager()
    private val activeState get() = keyboardManager.activeState
    private val breakIteratorGroup = BreakIteratorGroup(context)
    private fun currentInputConnection() = Vim8ImeService.currentInputConnection()

    var imeOptions: ImeOptions = ImeOptions.wrap(0)
        private set

    fun handleStartInputView(editorInfo: EditorInfo) {
        imeOptions = ImeOptions.wrap(editorInfo.imeOptions)

        activeState.imeUiMode = when (editorInfo.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> {
                activeState.keyVariation = KeyVariation.NORMAL
                ImeUiMode.NUMERIC
            }

            InputType.TYPE_CLASS_TEXT -> {
                activeState.keyVariation =
                    when (editorInfo.inputType and InputType.TYPE_MASK_VARIATION) {
                        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                        InputType.TYPE_TEXT_VARIATION_PASSWORD -> KeyVariation.PASSWORD

                        else -> KeyVariation.NORMAL
                    }
                ImeUiMode.TEXT
            }

            else -> {
                activeState.keyVariation = KeyVariation.NORMAL
                ImeUiMode.TEXT
            }
        }
    }

    fun commitText(text: String): Boolean {
        val ic = currentInputConnection() ?: return false
        return ic.commitText(text, 1)
    }

    fun performEnterAction(action: ImeOptions.Action): Boolean {
        val ic = currentInputConnection() ?: return false
        return ic.performEditorAction(action.toInt())
    }

    fun performEnter(): Boolean = sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0)

    fun performCut(): Boolean {
        val ic = currentInputConnection() ?: return false
        return ic.performContextMenuAction(android.R.id.cut)
    }

    fun performCopy(): Boolean {
        val ic = currentInputConnection() ?: return false
        return ic.performContextMenuAction(android.R.id.copy)
    }

    fun performPaste(): Boolean {
        val ic = currentInputConnection() ?: return false
        return ic.performContextMenuAction(android.R.id.paste)
    }

    fun performDelete(): Boolean {
        val ic = currentInputConnection() ?: return false
        return if ((ic.getSelectedText(0)?.length ?: 0) == 0) {
            val length =
                ic.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString()?.let { text ->
                    if (activeState.isCtrlOn) {
                        breakIteratorGroup.measureLastWords(text, 1)
                    } else {
                        breakIteratorGroup.measureLastCharacters(text, 1)
                    }.coerceAtLeast(1)
                } ?: 1
            ic.deleteSurroundingText(length, 0)
        } else {
            ic.commitText("", 0)
        }
    }

    fun performSwitchAnchor(): Boolean {
        val ic = currentInputConnection() ?: return false
        val extractedText =
            ic.getExtractedText(ExtractedTextRequest(), InputConnection.GET_EXTRACTED_TEXT_MONITOR)
        val start = extractedText.selectionStart
        val end = extractedText.selectionEnd
        return ic.setSelection(end, start)
    }

    fun sendDownAndUpKeyEvent(keyEventCode: Int, flags: Int): Boolean {
        val ic = currentInputConnection() ?: return false
        ic.sendDownKeyEvent(keyEventCode, flags)
        ic.sendUpKeyEvent(keyEventCode, flags)
        return true
    }

    private fun InputConnection.sendDownKeyEvent(keyEventCode: Int, flags: Int): Boolean =
        this.sendKeyEvent(
            KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                keyEventCode,
                0,
                flags
            )
        )

    private fun InputConnection.sendUpKeyEvent(keyEventCode: Int, flags: Int): Boolean =
        this.sendKeyEvent(
            KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                keyEventCode,
                0,
                flags
            )
        )
}
