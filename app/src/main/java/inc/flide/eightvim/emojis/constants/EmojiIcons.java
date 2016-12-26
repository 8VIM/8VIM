package inc.flide.eightvim.emojis.constants;

import java.util.ArrayList;

public abstract class EmojiIcons {

    protected ArrayList<Integer> transIconIds;
    protected ArrayList<Integer> thingsIconIds;
    protected ArrayList<Integer> peopleIconIds;
    protected ArrayList<Integer> otherIconIds;
    protected ArrayList<Integer> natureIconIds;

    public ArrayList<Integer> getNatureIconIds() {
        return natureIconIds;
    }

    public ArrayList<Integer> getOtherIconIds() {
        return otherIconIds;
    }

    public ArrayList<Integer> getPeopleIconIds() {
        return peopleIconIds;
    }

    public ArrayList<Integer> getThingsIconIds() {
        return thingsIconIds;
    }

    public ArrayList<Integer> getTransIconIds() {
        return transIconIds;
    }

}
