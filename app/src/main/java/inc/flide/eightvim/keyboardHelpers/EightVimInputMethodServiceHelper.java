package inc.flide.eightvim.keyboardHelpers;

import android.content.res.Resources;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keyboardActionMap;
    }
}
