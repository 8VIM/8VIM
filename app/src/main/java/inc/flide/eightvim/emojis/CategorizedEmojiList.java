package inc.flide.eightvim.emojis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import inc.flide.eightvim.structures.Emoji;

public class CategorizedEmojiList {

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

    public CategorizedEmojiList(List<Emoji> emojis) {
        emojis = removeDiversityEmojis(emojis);
        categorizeEmoji(emojis);
        sortEmoji();
    }

    private List<Emoji> removeDiversityEmojis(List<Emoji> emojis) {
        List<Emoji> emojiListMinusDiversity = new ArrayList<>();
        for(Emoji emoji: emojis){
            if(emoji.getName().indexOf("tone", 0) == -1) {
                emojiListMinusDiversity.add(emoji);
            }
        }
        return emojiListMinusDiversity;
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

    Comparator<Emoji> emojiComparator = new Comparator<Emoji>() {
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
}
