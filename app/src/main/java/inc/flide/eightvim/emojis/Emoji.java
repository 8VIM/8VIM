package inc.flide.eightvim.emojis;

import inc.flide.eightvim.structures.EmojiCategory;

public class Emoji {

    private String shortName;
    private String unicodeJavaString;
    private String unicodeHexcode;
    private EmojiCategory category;
    private int emojiOrder;

    public Emoji(String shortName, String unicodeJavaString, String unicodeHexcode, EmojiCategory category, int emojiOrder) {
        this.shortName = shortName;
        this.unicodeJavaString = unicodeJavaString;
        this.unicodeHexcode = unicodeHexcode;
        this.category = category;
        this.emojiOrder = emojiOrder;
    }

    public String getShortName() {
        return shortName;
    }

    public EmojiCategory getCategory() {
        return category;
    }

    public String getUnicodeHexcode() {
        return unicodeHexcode;
    }

    public String getUnicodeJavaString() {
        return unicodeJavaString;
    }

    public int getEmojiOrder() { return emojiOrder; }

}