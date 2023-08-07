package inc.flide.vim8.models

import arrow.optics.optics

@optics
data class KeyboardAction(
    val keyboardActionType: KeyboardActionType,
    val text: String,
    val capsLockText: String = "",
    val keyEventCode: Int,
    val keyFlags: Int,
    val layer: LayerLevel
) {
    companion object
}