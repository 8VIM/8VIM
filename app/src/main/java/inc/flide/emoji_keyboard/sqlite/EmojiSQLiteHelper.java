package inc.flide.emoji_keyboard.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EmojiSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_RECENTS = "recent";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UNICODE_HEX_CODE = "Unicode_Hex_Code";
    public static final String COLUMN_CATEGORY = "Category";
    public static final String COLUMN_COUNT = "Count";

    private static final String DATABASE_NAME = "RecentEmojis.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_RECENTS + "("
                                                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                                                    + COLUMN_UNICODE_HEX_CODE + " TEXT,"
                                                    + COLUMN_CATEGORY + " TEXT,"
                                                    + COLUMN_COUNT + " INTEGER" + ")";

    public EmojiSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTS);
            onCreate(db);
        }
    }

}