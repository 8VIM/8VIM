package inc.flide.eightvim.emojiSpecific;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import inc.flide.eightvim.EightVimInputMethodService;

public class StaticEmojiAdapter extends BaseEmojiAdapter {

    public StaticEmojiAdapter(Context context, String[] emojiTextsAsStrings, ArrayList<Integer> iconIds) {
        super((EightVimInputMethodService) context);
        this.emojiTexts =  new ArrayList<String>(Arrays.asList(emojiTextsAsStrings));
        this.iconIds = iconIds;
    }
}