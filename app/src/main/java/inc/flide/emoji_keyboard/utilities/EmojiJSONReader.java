package inc.flide.emoji_keyboard.utilities;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import inc.flide.emoji_keyboard.constants.EmojiCategory;

public class EmojiJSONReader {

    public static final String JSON_TAG_UNICODE = "unicode";
    public static final String JSON_TAG_UNICODE_ALT = "unicode_alt";
    public static final String JSON_TAG_CODE_DECIMAL = "code_decimal";
    public static final String JSON_TAG_NAME = "name";
    public static final String JSON_TAG_SHORTNAME = "shortname";
    public static final String JSON_TAG_CATEGORY = "category";
    public static final String JSON_TAG_EMOJI_ORDER = "emoji_order";
    public static final String JSON_TAG_ALIASES = "aliases";
    public static final String JSON_TAG_ALIASES_ASCII = "aliases_ascii";
    public static final String JSON_TAG_KEYWORDS = "keywords";

    private JsonReader reader;
    private InputStream inputStream;
    public EmojiJSONReader (InputStream inputStream) throws UnsupportedEncodingException {
        this.inputStream = inputStream;
    }

    public List<Emoji> loadEmojiData() throws IOException{
        reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        List<Emoji> emojiList = new ArrayList<>();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String shortname = reader.nextName();
                Emoji emoji = readEmoji();

                if (shortname.indexOf("_tone") == -1) {
                    emojiList.add(emoji);
                }
            }
            reader.endObject();
        } finally {
            reader.close();
        }
        return emojiList;
    }

    private Emoji readEmoji() throws IOException {
        String unicode = "";
        String unicode_alt;
        String code_decimal;
        String name = "";
        String shortname = "";
        String category = "";
        int emoji_order = 0;
        List<String> aliases;
        List<String> aliases_ascii;
        List<String> keywords = null;

        reader.beginObject();
        while (reader.hasNext()){
            String tag = reader.nextName();
            switch (tag) {
                case JSON_TAG_UNICODE:
                    unicode = reader.nextString();
                    break;
                case JSON_TAG_UNICODE_ALT:
                    reader.skipValue();
                    break;
                case JSON_TAG_CODE_DECIMAL:
                    reader.skipValue();
                    break;
                case JSON_TAG_NAME:
                    name = reader.nextString();
                    break;
                case JSON_TAG_SHORTNAME:
                    shortname = reader.nextString();
                    break;
                case JSON_TAG_CATEGORY:
                    category = reader.nextString();
                    break;
                case JSON_TAG_EMOJI_ORDER:
                    emoji_order = reader.nextInt();
                    break;
                case JSON_TAG_ALIASES:
                    reader.skipValue();
                    break;
                case JSON_TAG_ALIASES_ASCII:
                    reader.skipValue();
                    break;
                case JSON_TAG_KEYWORDS:
                    keywords = readStringArray();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return new Emoji(name, shortname, Utility.convertStringToUnicode(unicode), unicode, EmojiCategory.valueOf(category), emoji_order, keywords);

    }

    private List<String> readStringArray() throws IOException {
        List<String> strings = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()){
            strings.add(reader.nextString());
        }
        reader.endArray();

        return strings;
    }

}
