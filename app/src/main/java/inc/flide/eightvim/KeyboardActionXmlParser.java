package inc.flide.eightvim;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.logging.Logger;

/**
 * Created by flide on 24/11/15.
 */
public class KeyboardActionXmlParser {
    public static final String KEYBOARD_ACTION_MAP_TAG = "keyboardActionMap";
    public static final String KEYBOARD_ACTION_TAG = "keyboardAction";
    public static final String KEYBOARD_ACTION_TYPE_TAG = "keyboardActionType";
    public static final String MOVEMENT_SEQUENCE_TAG = "movementSequence";
    public static final String INPUT_STRING_TAG = "inputString";
    public static final String INPUT_KEY_TAG = "inputKey";

    XmlPullParser parser;

    public KeyboardActionXmlParser(InputStream inputStream) throws XmlPullParserException, IOException {
        parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);
        parser.nextTag();
    }

    public Map<List<FingerPosition>, KeyboardAction> readKeyboardActionMap() throws IOException, XmlPullParserException {
        Map<List<FingerPosition>, KeyboardAction> keyboardActionMap = new HashMap<>();

        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_ACTION_MAP_TAG);
        while (parser.next() != parser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if(tagName.equals(KEYBOARD_ACTION_TAG)){
                Map.Entry<List<FingerPosition>, KeyboardAction> keyboardAction = readKeyboardAction();
                keyboardActionMap.put(keyboardAction.getKey(), keyboardAction.getValue());
            }
        }
        return keyboardActionMap;
    }

    private Map.Entry<List<FingerPosition>, KeyboardAction> readKeyboardAction() throws XmlPullParserException, IOException {
        List<FingerPosition> movementSequence = null;
        KeyboardAction keyboardAction = null;
        KeyboardAction.KeyboardActionType keyboardActionType = null;
        String associatedText = "";
        int keyEventCode = 0;

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            switch (tagName) {
                case KEYBOARD_ACTION_TYPE_TAG:
                    keyboardActionType = readKeyboardActionType();
                    break;
                case MOVEMENT_SEQUENCE_TAG:
                    movementSequence = readMovementSequence();
                    break;
                case INPUT_STRING_TAG:
                    associatedText = readInputString();
                    break;
                case INPUT_KEY_TAG:
                    keyEventCode = readInputKey();
                    break;
                default:
                    Logger.w(this, "keyboard_actions xml has unknown tag : " + tagName);
            }
        }

        keyboardAction = new KeyboardAction(keyboardActionType, associatedText, keyEventCode);
        return new AbstractMap.SimpleEntry(movementSequence, keyboardAction);
    }

    private int readInputKey() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG,null, INPUT_KEY_TAG);
        String inputKeyString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_KEY_TAG);

        int keyEventCode = Integer.parseInt(inputKeyString);

        return keyEventCode;
    }

    private String readInputString() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG,null, INPUT_STRING_TAG);
        String inputString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_STRING_TAG);

        return inputString;
    }

    private List<FingerPosition> readMovementSequence() throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, null, MOVEMENT_SEQUENCE_TAG);
        String movementSequenceString = readText();
        parser.require(XmlPullParser.END_TAG, null, MOVEMENT_SEQUENCE_TAG);

        List<String> movementSequenceList = Arrays.asList(movementSequenceString.split("\\s*;\\s*"));
        List<FingerPosition> movementSequence = new ArrayList<>();
        for (String movement :movementSequenceList) {
            movementSequence.add(FingerPosition.valueOf(movement));
        }
        return movementSequence;
    }

    private KeyboardAction.KeyboardActionType readKeyboardActionType() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_ACTION_TYPE_TAG);
        String keyboardActionTypeString = readText();
        parser.require(XmlPullParser.END_TAG, null, KEYBOARD_ACTION_TYPE_TAG);

        KeyboardAction.KeyboardActionType keyboardActionType = KeyboardAction.KeyboardActionType.valueOf(keyboardActionTypeString);
        return keyboardActionType;
    }

    private String readText() throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
