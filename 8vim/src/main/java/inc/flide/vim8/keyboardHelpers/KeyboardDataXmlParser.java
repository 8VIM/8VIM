package inc.flide.vim8.keyboardHelpers;

import android.util.Xml;
import android.view.KeyEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;

class KeyboardDataXmlParser {
    private static final String KEYBOARD_DATA_TAG = "keyboardData";
    private static final String KEYBOARD_ACTION_MAP_TAG = "keyboardActionMap";
    private static final String KEYBOARD_ACTION_TAG = "keyboardAction";
    private static final String KEYBOARD_ACTION_TYPE_TAG = "keyboardActionType";
    private static final String MOVEMENT_SEQUENCE_TAG = "movementSequence";
    private static final String INPUT_STRING_TAG = "inputString";
    private static final String INPUT_CAPSLOCK_STRING_TAG = "inputCapsLockString";
    private static final String INPUT_KEY_TAG = "inputKey";
    private static final String INPUT_KEY_FLAGS_TAG = "flags";
    private static final String INPUT_KEY_FLAG_TAG = "flag";
    private static final String KEYBOARD_CHARACTER_SET_TAG = "keyboardCharacterSet";
    private static final String KEYBOARD_CHARACTER_SET_LOWERCASE_TAG = "lowerCase";
    private static final String KEYBOARD_CHARACTER_SET_UPPERCASE_TAG = "upperCase";

    private final XmlPullParser parser;

    KeyboardDataXmlParser(InputStream inputStream) throws XmlPullParserException, IOException {
        parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);
        parser.nextTag();
    }

    public KeyboardData readKeyboardData() throws IOException, XmlPullParserException {
        KeyboardData keyboardData = new KeyboardData();
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_DATA_TAG);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            switch (tagName) {
                case KEYBOARD_CHARACTER_SET_TAG:
                    int layer = readLayerAttribute();
                    readKeyboardCharacterSet(keyboardData, layer);
                    break;
                case KEYBOARD_ACTION_MAP_TAG:
                    Map<List<FingerPosition>, KeyboardAction> actionMap = readKeyboardActionMap();
                    keyboardData.setActionMap(actionMap);
                    break;
            }
        }
        return keyboardData;
    }

    private Map<List<FingerPosition>, KeyboardAction> readKeyboardActionMap() throws IOException, XmlPullParserException {
        Map<List<FingerPosition>, KeyboardAction> keyboardActionMap = new HashMap<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals(KEYBOARD_ACTION_TAG)) {
                int layer = readLayerAttribute();
                Map.Entry<List<FingerPosition>, KeyboardAction> keyboardAction = readKeyboardAction(layer);
                keyboardActionMap.put(keyboardAction.getKey(), keyboardAction.getValue());
            }
        }
        return keyboardActionMap;
    }

    private void readKeyboardCharacterSet(KeyboardData keyboardData, int layer) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            switch (tagName) {
                case KEYBOARD_CHARACTER_SET_LOWERCASE_TAG:
                    parser.require(XmlPullParser.START_TAG, null, KEYBOARD_CHARACTER_SET_LOWERCASE_TAG);
                    String keyboardLowerCaseCharacterSet = readText();
                    keyboardData.setLowerCaseCharacters(keyboardLowerCaseCharacterSet, layer);
                    parser.require(XmlPullParser.END_TAG, null, KEYBOARD_CHARACTER_SET_LOWERCASE_TAG);
                    break;
                case KEYBOARD_CHARACTER_SET_UPPERCASE_TAG:
                    parser.require(XmlPullParser.START_TAG, null, KEYBOARD_CHARACTER_SET_UPPERCASE_TAG);
                    String keyboardUpperCaseCharacterSet = readText();
                    keyboardData.setUpperCaseCharacters(keyboardUpperCaseCharacterSet, layer);
                    parser.require(XmlPullParser.END_TAG, null, KEYBOARD_CHARACTER_SET_UPPERCASE_TAG);
                    break;
                default:
            }
        }
    }

    private Map.Entry<List<FingerPosition>, KeyboardAction> readKeyboardAction(int layer) throws XmlPullParserException, IOException {
        List<FingerPosition> movementSequence = null;
        KeyboardAction keyboardAction;
        KeyboardActionType keyboardActionType = null;
        String associatedText = "";
        String associatedCapsLockText = "";
        int keyEventCode = 0;
        int flags = 0;
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
                case INPUT_CAPSLOCK_STRING_TAG:
                    associatedCapsLockText = readInputCapsLockString();
                    break;
                case INPUT_KEY_FLAGS_TAG:
                    flags = readInputFlags();
                    break;
                default:
                    //Logger.w(this, "keyboard_actions xml has unknown tag : " + tagName);
            }
        }
        keyboardAction = new KeyboardAction(keyboardActionType, associatedText, associatedCapsLockText, keyEventCode, flags, layer);
        return new AbstractMap.SimpleEntry<>(movementSequence, keyboardAction);
    }

    private int readLayerAttribute() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Optional
                    .ofNullable(parser.getAttributeValue(null, "layer"))
                    .map(id -> {
                        try {
                            return Integer.parseInt(id);
                        } catch (Exception e) {
                            return Constants.DEFAULT_LAYER;
                        }
                    })
                    .orElse(Constants.DEFAULT_LAYER);

        }
        return Constants.DEFAULT_LAYER;
    }

    private int readInputFlags() throws IOException, XmlPullParserException {

        int flags = 0;

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            switch (tagName) {
                case INPUT_KEY_FLAG_TAG:
                    flags = flags | readInputFlag();
                    break;
                default:
                    //Logger.w(this, "keyboard_actions xml has unknown tag : " + tagName);
            }
        }
        return flags;
    }

    private int readInputFlag() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, INPUT_KEY_FLAG_TAG);
        String inputKeyString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_KEY_FLAG_TAG);

        return Integer.parseInt(inputKeyString);
    }

    private int readInputKey() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, INPUT_KEY_TAG);
        String inputKeyString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_KEY_TAG);

        //Strictly the inputKey has to has to be a Keycode from the KeyEvent class
        //Or it needs to be one of the customKeyCodes
        int keyCode = KeyEvent.keyCodeFromString(inputKeyString);
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            try {
                keyCode = CustomKeycode.valueOf(inputKeyString).getKeyCode();
            } catch (IllegalArgumentException error) {
                keyCode = KeyEvent.KEYCODE_UNKNOWN;
            }
        }

        return keyCode;
    }

    private String readInputString() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, INPUT_STRING_TAG);
        String inputString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_STRING_TAG);

        return inputString;
    }

    private String readInputCapsLockString() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, INPUT_CAPSLOCK_STRING_TAG);
        String inputString = readText();
        parser.require(XmlPullParser.END_TAG, null, INPUT_CAPSLOCK_STRING_TAG);

        return inputString;
    }

    private List<FingerPosition> readMovementSequence() throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, null, MOVEMENT_SEQUENCE_TAG);
        String movementSequenceString = readText();
        parser.require(XmlPullParser.END_TAG, null, MOVEMENT_SEQUENCE_TAG);

        String[] movementSequenceList = movementSequenceString.split("\\s*;\\s*");
        List<FingerPosition> movementSequence = new ArrayList<>();
        for (String movement : movementSequenceList) {
            movementSequence.add(FingerPosition.valueOf(movement));
        }
        return movementSequence;
    }

    private KeyboardActionType readKeyboardActionType() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_ACTION_TYPE_TAG);
        String keyboardActionTypeString = readText();
        parser.require(XmlPullParser.END_TAG, null, KEYBOARD_ACTION_TYPE_TAG);

        return KeyboardActionType.valueOf(keyboardActionTypeString);
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
