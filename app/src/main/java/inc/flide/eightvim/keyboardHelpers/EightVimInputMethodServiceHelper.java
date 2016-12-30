package inc.flide.eightvim.keyboardHelpers;

import android.content.res.Resources;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.structures.Emoji;
import inc.flide.eightvim.structures.FingerPosition;

public class EightVimInputMethodServiceHelper {

    public Map<List<FingerPosition>, KeyboardAction> initializeKeyboardActionMap(Resources resources, String packageName) {

        Map<List<FingerPosition>, KeyboardAction> keyboardActionMap=null;

        InputStream inputStream = null;
        try{
            inputStream = resources.openRawResource(resources.getIdentifier("raw/keyboard_actions", "raw", packageName));
            KeyboardActionXmlParser keyboardActionXmlParser = new KeyboardActionXmlParser(inputStream);
            keyboardActionMap = keyboardActionXmlParser.readKeyboardActionMap();

        } catch (XmlPullParserException exception){
            exception.printStackTrace();
        } catch (IOException exception){
            exception.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keyboardActionMap;
    }

    public static List<Emoji> loadEmojiData(Resources resources, String packageName) {

        List<Emoji> emojiData = new ArrayList<>();
        InputStream inputStream = null;

        try {
            inputStream = resources.openRawResource(resources.getIdentifier("raw/emoji", "raw", packageName));
            EmojiJSONReader emojiJSONReader = new EmojiJSONReader(inputStream);
            emojiData = emojiJSONReader.loadEmojiData();
        } catch (UnsupportedEncodingException exception) {
            exception.printStackTrace();
        } catch (IOException exception){
            exception.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return emojiData;
    }
}
