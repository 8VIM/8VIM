package inc.flide.vim8.structures

import java.util.Locale

class KeyboardAction(
	val keyboardActionType: KeyboardActionType,
	val text: String?,
	capsLockText: String?,
	val keyEventCode: Int,
	val keyFlags: Int
) {
    val capsLockText: String? = if (capsLockText.isNullOrEmpty() && text != null) {
        text.uppercase(Locale.getDefault())
    } else {
        capsLockText
    }
}
