package inc.flide.vim8.ime.keyboard.xpad

import android.content.Context
import inc.flide.vim8.keyboardManager

class XpadManager(context: Context) {
    private val keyboardManager by context.keyboardManager()
    private val inputEventDispatcher get() = keyboardManager.inputEventDispatcher
}