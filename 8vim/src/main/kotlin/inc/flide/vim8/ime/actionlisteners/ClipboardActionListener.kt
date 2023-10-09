package inc.flide.vim8.ime.actionlisteners

import android.view.View
import inc.flide.vim8.MainInputMethodService

class ClipboardActionListener(mainInputMethodService: MainInputMethodService, view: View) :
    KeypadActionListener(mainInputMethodService, view) {
    val clipHistory: List<String>
        get() = mainInputMethodService.clipboardManagerService.clipHistory

    fun onClipSelected(selectedClip: String) {
        onText(selectedClip)
    }
}
