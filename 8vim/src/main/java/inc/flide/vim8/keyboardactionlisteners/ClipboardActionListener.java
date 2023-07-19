package inc.flide.vim8.keyboardactionlisteners;

import android.view.View;
import inc.flide.vim8.MainInputMethodService;
import java.util.List;


public class ClipboardActionListener extends KeypadActionListener {
    public ClipboardActionListener(MainInputMethodService mainInputMethodService, View view) {
        super(mainInputMethodService, view);
    }

    public List<String> getClipHistory() {
        return mainInputMethodService.getClipboardManagerService().getClipHistory();
    }

    public void onClipSelected(String selectedClip) {
        onText(selectedClip);
    }
}
