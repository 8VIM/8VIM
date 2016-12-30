package inc.flide.eightvim.emojis.adapter;

import android.content.Context;

import java.util.List;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.structures.Emoji;
import inc.flide.logging.Logger;

public class StaticEmojiAdapter extends BaseEmojiAdapter {

    private String filePrefix;
    public StaticEmojiAdapter(Context context, List<Emoji> emojiList, String filePrefix) {
        super((EightVimInputMethodService) context);
        this.emojiList = emojiList;
        this.filePrefix = filePrefix;
    }

    @Override
    public int getIconId(int position) {
        String resourceString = filePrefix + emojiList.get(position).getUnicodeHexcode().replace('-','_');
        int resourceId;
        resourceId = emojiKeyboardService.getResources().getIdentifier(resourceString, "drawable", emojiKeyboardService.getPackageName());
        if (resourceId == 0) {
            Logger.d(this, emojiList.get(position).getUnicodeHexcode());
            resourceId = emojiKeyboardService.getResources().getIdentifier("ic_not_available_sign", "drawable", emojiKeyboardService.getPackageName());
        }
        return resourceId;
    }

    @Override
    public String getEmojiUnicodeString(int position) {
        return emojiList.get(position).getUnicodeJavaString();
    }
}