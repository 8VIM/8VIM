package inc.flide.vim8.ime.keyboard.xpad

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import arrow.core.elementAtOrNone
import arrow.core.raise.nullable
import inc.flide.vim8.ime.layout.models.characterSets

class Key(val index: Int, private val keyboard: Keyboard) {
    var position: Offset by mutableStateOf(Offset.Zero)
    var characterHeightWidth: Float by mutableStateOf(0f)
    var isSelected: Boolean by mutableStateOf(false)

    fun text(isCapitalize: Boolean): String = nullable {
        val keyboardData = ensureNotNull(keyboard.keyboardData)
        val characterSets = keyboardData.characterSets(keyboard.layerLevel).bind()
        val action = characterSets.elementAtOrNone(index).bind()
        val keyboardAction = ensureNotNull(action)
        if (isCapitalize) keyboardAction.capsLockText else keyboardAction.text
    }.orEmpty()

}