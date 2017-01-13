package inc.flide.emoji_keyboard.utilities;

import java.util.List;

import inc.flide.emoji_keyboard.constants.EmojiCategory;

public class Emoji {

    private String name;
    private String shortName;
    private String unicodeJavaString;
    private String unicodeHexcode;
    private EmojiCategory category;
    private List<String> keywords;
    private int emojiOrder;
    private boolean isDiversityAvailable;

    public Emoji(String name, String shortName, String unicodeJavaString, String unicodeHexcode, EmojiCategory category, int emojiOrder, List<String> keywords) {
        this.name = name;
        this.shortName = shortName;
        this.unicodeJavaString = unicodeJavaString;
        this.unicodeHexcode = unicodeHexcode;
        this.category = category;
        this.emojiOrder = emojiOrder;
        this.keywords = keywords;
        setDiversity();
    }

    public Emoji(Emoji copy) {
        this.name = copy.name;
        this.shortName = copy.shortName;
        this.unicodeJavaString = copy.unicodeJavaString;
        this.unicodeHexcode = copy.unicodeHexcode;
        this.category = copy.category;
        this.emojiOrder = copy.emojiOrder;
        this.keywords = copy.keywords;
        this.isDiversityAvailable = copy.isDiversityAvailable;
    }

    private void setDiversity() {
        isDiversityAvailable = false;
        for(String keyword: keywords) {
            if(keyword.equalsIgnoreCase("diversity")){
                isDiversityAvailable = true;
            }
        }
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

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean isDiversityAvailable() {
        return isDiversityAvailable;
    }

    public void setCategory(EmojiCategory category) {
        this.category = category;
    }

    public void setEmojiOrder(int emojiOrder) {
        this.emojiOrder = emojiOrder;
    }

    public void setDiversityAvailable(boolean diversityAvailable) {
        isDiversityAvailable = diversityAvailable;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setUnicodeHexcode(String unicodeHexcode) {
        this.unicodeHexcode = unicodeHexcode;
    }

    public void setUnicodeJavaString(String unicodeJavaString) {
        this.unicodeJavaString = unicodeJavaString;
    }

}