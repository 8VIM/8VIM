package inc.flide.emoji_keyboard.view;

import android.view.View;

import inc.flide.emoji_keyboard.InputMethodServiceProxy;
import inc.flide.emoji_keyboard.sqlite.EmojiDataSource;
import inc.flide.emoji_keyboard.utilities.CategorizedEmojiList;
import inc.flide.emoji_keyboard.utilities.Emoji;

public class EmojiOnClickListner implements View.OnClickListener {

    private Emoji emoji;
    private InputMethodServiceProxy inputMethodService;

    public EmojiOnClickListner(Emoji emoji, InputMethodServiceProxy inputMethodService) {
        this.emoji = emoji;
        this.inputMethodService = inputMethodService;
    }

    @Override
    public void onClick(View view) {
        inputMethodService.sendText(emoji.getUnicodeJavaString());

        EmojiDataSource.getInstance(inputMethodService.getContext())
                .addEntry(
                    CategorizedEmojiList.getInstance().searchForEmojiIgnoreModifier(emoji.getUnicodeHexcode(), emoji.getCategory().toString())
                );
    }
}
