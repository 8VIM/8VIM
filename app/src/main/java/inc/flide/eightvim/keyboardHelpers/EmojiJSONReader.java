package inc.flide.eightvim.keyboardHelpers;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import inc.flide.eightvim.emojis.Emoji;
import inc.flide.eightvim.structures.EmojiCategory;
import inc.flide.logging.Logger;

/*
{
    "grinning":
        {
        "unicode":"1f600",
        "unicode_alt":"",
        "code_decimal":"&#128512;",
        "name":"grinning face",
        "shortname":":grinning:",
        "category":"people",
        "emoji_order":"1",
        "aliases":[],
        "aliases_ascii":[],
        "keywords":["happy","smiley","emotion"]
        },
     "grin":
        {"unicode":"1f601","unicode_alt":"","code_decimal":"&#128513;","name":"grinning face with smiling eyes","shortname":":grin:","category":"people","emoji_order":"2","aliases":[],"aliases_ascii":[],"keywords":["happy","silly","smiley","emotion","good","selfie"]
        },
     "joy":
        {"unicode":"1f602","unicode_alt":"","code_decimal":"&#128514;","name":"face with tears of joy","shortname":":joy:","category":"people","emoji_order":"3","aliases":[],"aliases_ascii":[":')",":'-)"],"keywords":["happy","silly","smiley","cry","laugh","emotion","sarcastic"]
        }
}
*/
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
                reader.nextName();
                emojiList.add(readEmoji());
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
        String name;
        String shortname = "";
        String category = "";
        int emoji_order = 0;
        List<String> aliases;
        List<String> aliases_ascii;
        List<String> keywords;

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
                    reader.skipValue();
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
                    reader.skipValue();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return new Emoji(shortname, convertStringToUnicode(unicode), unicode, EmojiCategory.valueOf(category), emoji_order);

    }


    private String convertStringToUnicode(String unicode) {

        String[] unicodeParts = unicode.split("-");
        int[] codePoints = new int[unicodeParts.length];

        for (int i = 0 ; i<unicodeParts.length; i++){
            codePoints[i] = Integer.valueOf(unicodeParts[i], 16);
        }

        int offset = 0;
        int count = unicodeParts.length;

        return new String(codePoints, offset, count);
    }

}
