package inc.flide.vim8.ime.actionlisteners

import android.view.View
import inc.flide.vim8.Vim8ImeService

class ClipboardActionListener(vim8ImeService: Vim8ImeService, view: View) :
    KeypadActionListener(vim8ImeService, view) {
    val clipHistory: List<String> = emptyList()
//        get() = vim8ImeService.clipboardManagerService.clipHistory

    fun onClipSelected(selectedClip: String) {
        onText(selectedClip)
    }
}
