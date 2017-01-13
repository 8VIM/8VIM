package inc.flide.emoji_keyboard.view;

import android.view.View;
import android.widget.PopupWindow;

import inc.flide.emoji_keyboard.InputMethodServiceProxy;
import inc.flide.emoji_keyboard.sqlite.EmojiDataSource;
import inc.flide.emoji_keyboard.utilities.Emoji;

public class PopupWindowEmojiOnClickListner extends EmojiOnClickListner {

    private PopupWindow popupWindow;
    public PopupWindowEmojiOnClickListner(Emoji emoji, InputMethodServiceProxy inputMethodService, final PopupWindow popupWindow) {
        super(emoji, inputMethodService);
        this.popupWindow = popupWindow;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        popupWindow.dismiss();
    }

}
