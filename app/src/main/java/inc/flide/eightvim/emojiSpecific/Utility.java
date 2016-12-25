package inc.flide.eightvim.emojiSpecific;

import java.util.ArrayList;

public class Utility {
    public static final ArrayList<Integer> initArrayList(int... ints) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i : ints)
        {
            list.add(i);
        }
        return list;
    }
}
