package inc.flide.vim8.ime.keyboard.text

import android.view.KeyEvent
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API29_Q
import inc.flide.vim8.lib.geometry.Rect

private val minCode = CustomKeycode.entries.minBy { it.keyCode }.keyCode
private val maxCode = if (ATLEAST_API29_Q) KeyEvent.KEYCODE_PROFILE_SWITCH else 288
private val keyboardActionCache: Map<Int, KeyboardAction> =
    (minCode..maxCode).associateBy({ it }, {
        KeyboardAction(
            keyboardActionType = KeyboardActionType.INPUT_KEY,
            text = "",
            keyEventCode = it,
            keyFlags = 0,
            layer = LayerLevel.FIRST
        )
    })

class Key(
    val action: KeyboardAction,
    val alternateText: String? = null,
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

fun Int.toKeyboardAction(): KeyboardAction = keyboardActionCache[this] ?: KeyboardAction(
    keyboardActionType = KeyboardActionType.INPUT_KEY,
    text = "",
    keyEventCode = this,
    keyFlags = 0,
    layer = LayerLevel.FIRST
)

fun CustomKeycode.toKeyboardAction(): KeyboardAction = keyCode.toKeyboardAction()
