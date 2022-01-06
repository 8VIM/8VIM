package inc.flide.vim8.structures

import java.util.*

class KeyboardAction(private val keyboardActionType: KeyboardActionType?, private val text: String?, capsLockText: String?, private val keyEventCode: Int, keyFlags: Int) {
    private var capsLockText: String? = null
    private val keyFlags: Int
    fun getKeyFlags(): Int {
        return keyFlags
    }

    fun getText(): String? {
        return text
    }

    fun getKeyEventCode(): Int {
        return keyEventCode
    }

    fun getKeyboardActionType(): KeyboardActionType? {
        return keyboardActionType
    }

    fun getCapsLockText(): String? {
        return capsLockText
    }

    private fun setCapsLockText(capsLockText: String?) {
        if ((capsLockText == null || capsLockText.isEmpty()) && text != null) {
            this.capsLockText = text.uppercase(Locale.getDefault())
        } else {
            this.capsLockText = capsLockText
        }
    }

    init {
        setCapsLockText(capsLockText)
        this.keyFlags = keyFlags
    }
}