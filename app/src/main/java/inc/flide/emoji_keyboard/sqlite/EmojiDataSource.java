package inc.flide.emoji_keyboard.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.orhanobut.logger.Logger;

import inc.flide.emoji_keyboard.utilities.CategorizedEmojiList;
import inc.flide.emoji_keyboard.utilities.Emoji;

public class EmojiDataSource {
    private static final int NUM_RECENTS_TO_SAVE = 60;

    private static EmojiDataSource instance = null;

    // Database fields
    private SQLiteDatabase database;
    private EmojiSQLiteHelper databaseHelper;
    private String[] allColumns = { EmojiSQLiteHelper.COLUMN_ID,
            EmojiSQLiteHelper.COLUMN_UNICODE_HEX_CODE, EmojiSQLiteHelper.COLUMN_CATEGORY, EmojiSQLiteHelper.COLUMN_COUNT };

    public static final EmojiDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new EmojiDataSource(context);
        }
        return instance;
    }

    private EmojiDataSource(Context context) {
        databaseHelper = new EmojiSQLiteHelper(context);
        openInReadWriteMode();
    }

    public void openInReadWriteMode() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
    }

    private ContentValues getFilledContentValuesObject(String unicodeHexValue, String category, long count) {

        ContentValues values = new ContentValues();
        values.put(EmojiSQLiteHelper.COLUMN_UNICODE_HEX_CODE, unicodeHexValue);
        values.put(EmojiSQLiteHelper.COLUMN_CATEGORY, category);
        values.put(EmojiSQLiteHelper.COLUMN_COUNT, count);

        return values;
    }

    private ContentValues getFilledContentValuesObject(RecentEntry recentEntry) {
        return getFilledContentValuesObject(recentEntry.getEmoji().getUnicodeHexcode(), recentEntry.getEmoji().getCategory().name(), recentEntry.getCount());
    }

    public void addEntry(Emoji emoji) {

        Cursor cursor = database.query(EmojiSQLiteHelper.TABLE_RECENTS,
                allColumns, EmojiSQLiteHelper.COLUMN_UNICODE_HEX_CODE + " = '" + CategorizedEmojiList.getInstance().removeModifierIfPresent(emoji.getUnicodeHexcode()) + "'", null,
                null, null, null);
        if (cursor.getCount() == 0) {
            insertNewEntry(emoji);
        } else {
            cursor.moveToFirst();
            RecentEntry newRecentEntry = cursorToRecent(cursor);
            cursor.close();
            incrementExistingEntryCountbyOne(newRecentEntry);
        }
    }
    private RecentEntry insertNewEntry(Emoji emoji) {

        int initialCount = 0;
        ContentValues values = getFilledContentValuesObject(emoji.getUnicodeHexcode(), emoji.getCategory().name(), initialCount);
        long insertId = database.insert(EmojiSQLiteHelper.TABLE_RECENTS, null, values);

        if (insertId == -1) {
            return null;
        } else {
            return new RecentEntry(emoji, initialCount, insertId);
        }
    }

    private void incrementExistingEntryCountbyOne(RecentEntry entry) {
        entry.incrementUsageCountByOne();
        ContentValues values = getFilledContentValuesObject(entry);
        database.update(EmojiSQLiteHelper.TABLE_RECENTS, values, EmojiSQLiteHelper.COLUMN_ID +"="+ entry.getId(), null);
    }

    public boolean deleteEntryWithId(long id) {

        int rowsDeleted = database.delete(EmojiSQLiteHelper.TABLE_RECENTS, EmojiSQLiteHelper.COLUMN_ID + " = " + id, null);

        if(rowsDeleted == 0) {
            return false;
        } else {
            return true;
        }
    }

    public List<Emoji> getAllEntriesInDescendingOrderOfCount() {
        List<Emoji> recentEntries = new ArrayList<>();
        if(!database.isOpen()) {
            this.openInReadWriteMode();
        }
        Cursor cursor = database.query(EmojiSQLiteHelper.TABLE_RECENTS,
                allColumns, null, null, null, null, EmojiSQLiteHelper.COLUMN_COUNT + " * 1 DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getPosition() >= NUM_RECENTS_TO_SAVE) {
                deleteEntryWithId(cursor.getLong(cursor.getColumnIndexOrThrow(EmojiSQLiteHelper.COLUMN_ID)));
            } else {
                RecentEntry recentEntry = cursorToRecent(cursor);
                recentEntries.add(recentEntry.getEmoji());
            }

            cursor.moveToNext();
        }

        cursor.close();
        return recentEntries;
    }

    private RecentEntry cursorToRecent(Cursor cursor) {

        Emoji emoji = CategorizedEmojiList.getInstance().searchForEmoji(
                                                        cursor.getString(cursor.getColumnIndexOrThrow(EmojiSQLiteHelper.COLUMN_UNICODE_HEX_CODE)),
                                                        cursor.getString(cursor.getColumnIndexOrThrow(EmojiSQLiteHelper.COLUMN_CATEGORY)));
        return new RecentEntry( emoji,
                                cursor.getLong(cursor.getColumnIndexOrThrow(EmojiSQLiteHelper.COLUMN_COUNT)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(EmojiSQLiteHelper.COLUMN_ID)));
    }
}