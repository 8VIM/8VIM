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
import java.util.Objects;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.utils.QuadrantHelper;

class KeyboardDataXmlParser {
    private static final String KEYBOARD_DATA_TAG = "keyboardData";
    private static final String KEYBOARD_ACTION_TAG = "keyboardAction";
    private static final String KEYBOARD_ACTION_TYPE_TAG = "keyboardActionType";
    private static final String MOVEMENT_SEQUENCE_TAG = "movementSequence";
    private static final String INPUT_STRING_TAG = "inputString";
    private static final String INPUT_CAPSLOCK_STRING_TAG = "inputCapsLockString";
    private static final String INPUT_KEY_TAG = "inputKey";
    private static final String INPUT_KEY_FLAGS_TAG = "flags";
    private static final String INPUT_KEY_FLAG_TAG = "flag";
    private static final String LAYER_TAG = "layer";
    private static final String SECTOR_TAG = "sector";
    private static final String LAYER_LEVEL_ATTRIBUTE = "level";
    private static final String QUADRANT_ATTRIBUTE = "quadrant";
    private static final String POSITION_ATTRIBUTE = "position";

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
                case LAYER_TAG:
                    int layer = readLayerLevelAttribute();
                    readLayer(keyboardData, layer);
                    break;
            }
        }
        return keyboardData;
    }

    private void readLayer(KeyboardData keyboardData, int layer) throws IOException, XmlPullParserException {
        StringBuilder lowerCaseCharacters = new StringBuilder();
        StringBuilder upperCaseCharacters = new StringBuilder();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals(SECTOR_TAG)) {
                Quadrant quadrant = readQuadrantAttribute();
                Objects.requireNonNull(quadrant);
                Map<List<FingerPosition>, KeyboardAction> actionMap =
                    readKeyboardActionMap(layer, quadrant, lowerCaseCharacters, upperCaseCharacters);
                keyboardData.addAllToActionMap(actionMap);
            }
        }
        if (layer >= Constants.DEFAULT_LAYER) {
            keyboardData.setLowerCaseCharacters(String.valueOf(lowerCaseCharacters), layer);
            keyboardData.setUpperCaseCharacters(String.valueOf(upperCaseCharacters), layer);
        }
    }

    private Map<List<FingerPosition>, KeyboardAction> readKeyboardActionMap(int layer, Quadrant quadrant, StringBuilder lowerCaseCharacters,
                                                                            StringBuilder upperCaseCharacters)
        throws IOException, XmlPullParserException {
        Map<List<FingerPosition>, KeyboardAction> keyboardActionMap = new HashMap<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals(KEYBOARD_ACTION_TAG)) {
                int position = readPositionAttribute();
                int characterSetIndex = getCharacterSetIndex(quadrant, position);
                List<FingerPosition> movementSequence = QuadrantHelper.computeMovementSequence(layer, quadrant, position);
                Map.Entry<List<FingerPosition>, KeyboardAction> keyboardAction =
                    readKeyboardAction(layer, characterSetIndex, movementSequence, lowerCaseCharacters, upperCaseCharacters);
                keyboardActionMap.put(keyboardAction.getKey(), keyboardAction.getValue());
            }
        }
        return keyboardActionMap;
    }


    private Map.Entry<List<FingerPosition>, KeyboardAction> readKeyboardAction(int layer, int index, List<FingerPosition> computedMovementSequence,
                                                                               StringBuilder lowerCaseCharacters, StringBuilder upperCaseCharacters)
        throws XmlPullParserException, IOException {
        List<FingerPosition> movementSequence = null;
        KeyboardAction keyboardAction;
        KeyboardActionType keyboardActionType = null;
        String associatedText = "";
        String associatedCapsLockText = "";
        int keyEventCode = 0;
        int flags = 0;

        if (!computedMovementSequence.isEmpty()) {
            movementSequence = computedMovementSequence;
        }

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
                    if (lowerCaseCharacters.length() == 0) {
                        lowerCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                    }
                    lowerCaseCharacters.setCharAt(index, associatedText.charAt(0));
                    break;
                case INPUT_KEY_TAG:
                    keyEventCode = readInputKey();
                    break;
                case INPUT_CAPSLOCK_STRING_TAG:
                    associatedCapsLockText = readInputCapsLockString();
                    if (upperCaseCharacters.length() == 0) {
                        upperCaseCharacters.setLength(Constants.CHARACTER_SET_SIZE);
                    }
                    upperCaseCharacters.setCharAt(index, associatedCapsLockText.charAt(0));
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

    public Quadrant readQuadrantAttribute() {
        String quadrant = parser.getAttributeValue(null, QUADRANT_ATTRIBUTE);

        if (quadrant == null) {
            return null;
        }

        return Quadrant.valueOf(quadrant);
    }

    private int readLayerLevelAttribute() {
        String id = parser.getAttributeValue(null, LAYER_LEVEL_ATTRIBUTE);

        if (id == null) {
            return Constants.DEFAULT_LAYER;
        }

        try {
            return Integer.parseInt(id);
        } catch (Exception e) {
            return Constants.DEFAULT_LAYER;
        }
    }

    private int readPositionAttribute() {
        String position = parser.getAttributeValue(null, POSITION_ATTRIBUTE);

        if (position == null) {
            return Constants.DEFAULT_POSITION;
        }

        try {
            return Integer.parseInt(position) - 1;
        } catch (Exception e) {
            return Constants.DEFAULT_POSITION;
        }
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

    private int getCharacterSetIndex(Quadrant quadrant, int position) {
        int base = quadrant.ordinal() / 2 * 8;
        int delta = quadrant.ordinal() % 2;
        return base + position * 2 + delta;
    }
}
