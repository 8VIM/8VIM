package inc.flide.vim8.ime.keyboard.text

import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.lib.Rect

class Key(
    val action: KeyboardAction,
    val drawableId: Int? = null
) {
    val touchBounds: Rect = Rect.empty()
    val visibleBounds: Rect = Rect.empty()
}

fun String.toKeyboardAction(): KeyboardAction = KeyboardAction(
    keyboardActionType = KeyboardActionType.INPUT_TEXT,
    text = this,
    keyEventCode = 0,
    keyFlags = 0,
    layer = LayerLevel.FIRST
)

fun Int.toKeyboardAction(): KeyboardAction = KeyboardAction(
    keyboardActionType = KeyboardActionType.INPUT_KEY,
    text = "",
    keyEventCode = this,
    keyFlags = 0,
    layer = LayerLevel.FIRST
)

fun CustomKeycode.toKeyboardAction(): KeyboardAction = KeyboardAction(
    keyboardActionType = KeyboardActionType.INPUT_KEY,
    text = "",
    keyEventCode = this.keyCode,
    keyFlags = 0,
    layer = LayerLevel.FIRST
)
