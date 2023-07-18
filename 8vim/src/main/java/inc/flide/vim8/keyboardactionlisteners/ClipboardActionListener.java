package inc.flide.vim8.keyboardactionlisteners;

import android.view.View;

import java.util.Set;

import inc.flide.vim8.MainInputMethodService;

public class ClipboardActionListener extends KeypadActionListener{
    public ClipboardActionListener(MainInputMethodService mainInputMethodService, View view) {
        super(mainInputMethodService, view);
    }

    public Set<String> getClipHistory() {
       return mainInputMethodService.getClipboardManagerService().getClipHistory();
    }

    public void onClipSelected(String selectedClip) {
        onText(selectedClip);
    }
}
