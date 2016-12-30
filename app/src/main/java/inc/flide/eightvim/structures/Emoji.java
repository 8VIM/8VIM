package inc.flide.eightvim.structures;

import inc.flide.eightvim.structures.EmojiCategory;

public class Emoji {

    private String name;
    private String shortName;
    private String unicodeJavaString;
    private String unicodeHexcode;
    private EmojiCategory category;
    private int emojiOrder;

    public Emoji(String name, String shortName, String unicodeJavaString, String unicodeHexcode, EmojiCategory category, int emojiOrder) {
        this.name = name;
        this.shortName = shortName;
        this.unicodeJavaString = unicodeJavaString;
        this.unicodeHexcode = unicodeHexcode;
        this.category = category;
        this.emojiOrder = emojiOrder;
    }

    public String getName() {
        return name;
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