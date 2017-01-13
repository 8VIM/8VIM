package inc.flide.emoji_keyboard.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import inc.flide.emoji_keyboard.constants.EmojiCategory;

public class CategorizedEmojiList {

    private static CategorizedEmojiList instance = null;

    private List<Emoji> people = new ArrayList<>();
    private List<Emoji> nature = new ArrayList<>();
    private List<Emoji> activity = new ArrayList<>();
    private List<Emoji> food = new ArrayList<>();
    private List<Emoji> travel = new ArrayList<>();
    private List<Emoji> objects = new ArrayList<>();
    private List<Emoji> symbols = new ArrayList<>();
    private List<Emoji> flags = new ArrayList<>();
    private List<Emoji> modifier = new ArrayList<>();
    private List<Emoji> regional = new ArrayList<>();

    public static final CategorizedEmojiList getInstance() {
        if (instance == null) {
            instance = new CategorizedEmojiList();
        }

        return instance;
    }
    private CategorizedEmojiList() {}

    public List<Emoji> getDiversityEmojisList(Emoji primaryEmoji) {
        List<Emoji> diversityEmojiList = new ArrayList<>();
        for (Emoji emoji : modifier) {
            Emoji diversity = new Emoji(primaryEmoji);
            diversity.setUnicodeHexcode(diversity.getUnicodeHexcode() + "-" + emoji.getUnicodeHexcode());
            diversity.setUnicodeJavaString(Utility.convertStringToUnicode(diversity.getUnicodeHexcode()));
            diversityEmojiList.add(diversity);
        }
        return diversityEmojiList;
    }

    public void initializeCategoziedEmojiList(List<Emoji> emojis){
        resetAllEmojiLists();
        categorizeEmoji(emojis);
        sortEmoji();
    }

    public List<Emoji> getActivity() {
        return activity;
    }

    public List<Emoji> getFlags() {
        return flags;
    }

    public List<Emoji> getFood() {
        return food;
    }

    public List<Emoji> getModifier() {
        return modifier;
    }

    public List<Emoji> getNature() {
        return nature;
    }

    public List<Emoji> getObjects() {
        return objects;
    }

    public List<Emoji> getPeople() {
        return people;
    }

    public List<Emoji> getRegional() {
        return regional;
    }

    public List<Emoji> getSymbols() {
        return symbols;
    }

    public List<Emoji> getTravel() {
        return travel;
    }

    private void resetAllEmojiLists() {
        people.clear();
        nature.clear();
        activity.clear();
        food.clear();
        travel.clear();
        objects.clear();
        symbols.clear();
        flags.clear();
        modifier.clear();
        regional.clear();
    }

    private void sortEmoji() {
        Collections.sort(people, emojiComparator);
        Collections.sort(nature, emojiComparator);
        Collections.sort(activity, emojiComparator);
        Collections.sort(food, emojiComparator);
        Collections.sort(travel, emojiComparator);
        Collections.sort(objects, emojiComparator);
        Collections.sort(symbols, emojiComparator);
        Collections.sort(flags, emojiComparator);
        Collections.sort(modifier, emojiComparator);
        Collections.sort(regional, emojiComparator);
    }

    private Comparator<Emoji> emojiComparator = new Comparator<Emoji>() {
        @Override
        public int compare(Emoji first, Emoji second) {
            return first.getEmojiOrder() - second.getEmojiOrder();
        }
    };

    private void categorizeEmoji(List<Emoji> emojis) {
        for(Emoji emoji: emojis) {
            switch (emoji.getCategory()) {
                case people:
                    people.add(emoji);
                    break;
                case nature:
                    nature.add(emoji);
                    break;
                case activity:
                    activity.add(emoji);
                    break;
                case food:
                    food.add(emoji);
                    break;
                case travel:
                    travel.add(emoji);
                    break;
                case objects:
                    objects.add(emoji);
                    break;
                case symbols:
                    symbols.add(emoji);
                    break;
                case flags:
                    flags.add(emoji);
                    break;
                case modifier:
                    modifier.add(emoji);
                    break;
                case regional:
                    regional.add(emoji);
                    break;
            }
        }
    }

    public Emoji searchForEmojiIgnoreModifier(String unicodeHexValue, String category) {
        String unicodeHexValueWithoutModifier = removeModifierIfPresent(unicodeHexValue);
        Emoji returnValue = searchForEmoji(unicodeHexValueWithoutModifier, category);
        return returnValue;
    }

    public String removeModifierIfPresent(String unicodeHexValue) {
        String modifierRemovedUnicodeHexValue = unicodeHexValue;
        for(Emoji mod : modifier) {
            int indexOfModifier = unicodeHexValue.indexOf(mod.getUnicodeHexcode());
            if(indexOfModifier != -1){
                modifierRemovedUnicodeHexValue = unicodeHexValue.substring(0, indexOfModifier-1);
            }
        }
        return modifierRemovedUnicodeHexValue;
    }

    public Emoji searchForEmoji(String unicodeHexValue, String category) {
        EmojiCategory emojiCategory = EmojiCategory.valueOf(category);
        List<Emoji> searchableEmojiList = new ArrayList<>();
        switch (emojiCategory) {
            case people:
                searchableEmojiList = people;
                break;
            case nature:
                searchableEmojiList = nature;
                break;
            case activity:
                searchableEmojiList = activity;
                break;
            case food:
                searchableEmojiList = food;
                break;
            case travel:
                searchableEmojiList = travel;
                break;
            case objects:
                searchableEmojiList = objects;
                break;
            case symbols:
                searchableEmojiList = symbols;
                break;
            case flags:
                searchableEmojiList = flags;
                break;
            case modifier:
                searchableEmojiList = modifier;
                break;
            case regional:
                searchableEmojiList = regional;
                break;
        }

        Emoji result = null;
        for (Emoji emoji : searchableEmojiList){
            if(emoji.getUnicodeHexcode().compareToIgnoreCase(unicodeHexValue)==0) {
                result = emoji;
                break;
            }
        }

        return result;
    }
}
