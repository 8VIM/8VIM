package inc.flide.emoji_keyboard.sqlite;

import inc.flide.emoji_keyboard.utilities.Emoji;

public class RecentEntry {

    private long id;
    private Emoji emoji;
    private long count;

    public void incrementUsageCountByOne() {
        this.count = this.count+1;
    }

    public RecentEntry(Emoji emoji, long count, long id) {
        this.count = count;
        this.id = id;
        this.emoji = emoji;
    }

    public long getId() {
        return id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public void setEmoji(Emoji emoji) {
        this.emoji = emoji;
    }
}
