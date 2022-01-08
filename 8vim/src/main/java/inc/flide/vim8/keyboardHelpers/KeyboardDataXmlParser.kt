package inc.flide.vim8.keyboardHelpers

import android.util.Xml
import android.view.KeyEvent
import inc.flide.vim8.structures.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*

internal class KeyboardDataXmlParser(inputStream: InputStream) {
    private val parser: XmlPullParser = Xml.newPullParser()

    @Throws(IOException::class, XmlPullParserException::class)
    fun readKeyboardData(): KeyboardData {
        val keyboardData = KeyboardData()
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_DATA_TAG)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                KEYBOARD_CHARACTER_SET_TAG -> readKeyboardCharacterSet(keyboardData)
                KEYBOARD_ACTION_MAP_TAG -> keyboardData.setActionMap(readKeyboardActionMap())
            }
        }
        return keyboardData
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readKeyboardActionMap(): MutableMap<MutableList<FingerPosition>?, KeyboardAction?> {
        val keyboardActionMap: MutableMap<MutableList<FingerPosition>?, KeyboardAction?> = HashMap()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.name
            if (tagName == KEYBOARD_ACTION_TAG) {
                val keyboardAction = readKeyboardAction()
                keyboardActionMap[keyboardAction.key] = keyboardAction.value
            }
        }
        return keyboardActionMap
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readKeyboardCharacterSet(keyboardData: KeyboardData) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                KEYBOARD_CHARACTER_SET_LOWERCASE_TAG -> {
                    parser.require(XmlPullParser.START_TAG, null, KEYBOARD_CHARACTER_SET_LOWERCASE_TAG)
                    val keyboardLowerCaseCharacterSet = readText()
                    keyboardData.setLowerCaseCharacters(keyboardLowerCaseCharacterSet)
                    parser.require(XmlPullParser.END_TAG, null, KEYBOARD_CHARACTER_SET_LOWERCASE_TAG)
                }
                KEYBOARD_CHARACTER_SET_UPPERCASE_TAG -> {
                    parser.require(XmlPullParser.START_TAG, null, KEYBOARD_CHARACTER_SET_UPPERCASE_TAG)
                    val keyboardUpperCaseCharacterSet = readText()
                    keyboardData.setUpperCaseCharacters(keyboardUpperCaseCharacterSet)
                    parser.require(XmlPullParser.END_TAG, null, KEYBOARD_CHARACTER_SET_UPPERCASE_TAG)
                }
                else -> {}
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readKeyboardAction(): MutableMap.MutableEntry<MutableList<FingerPosition>?, KeyboardAction?> {
        var movementSequence: MutableList<FingerPosition>? = null
        val keyboardAction: KeyboardAction
        var keyboardActionType: KeyboardActionType? = null
        var associatedText: String? = ""
        var associatedCapsLockText: String? = ""
        var keyEventCode = 0
        var flags = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                KEYBOARD_ACTION_TYPE_TAG -> keyboardActionType = readKeyboardActionType()
                MOVEMENT_SEQUENCE_TAG -> movementSequence = readMovementSequence()
                INPUT_STRING_TAG -> associatedText = readInputString()
                INPUT_KEY_TAG -> keyEventCode = readInputKey()
                INPUT_CAPSLOCK_STRING_TAG -> associatedCapsLockText = readInputCapsLockString()
                INPUT_KEY_FLAGS_TAG -> flags = readInputFlags()
                else -> {}
            }
        }
        keyboardAction = KeyboardAction(keyboardActionType, associatedText, associatedCapsLockText, keyEventCode, flags)
        return AbstractMap.SimpleEntry(movementSequence, keyboardAction)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputFlags(): Int {
        var flags = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                INPUT_KEY_FLAG_TAG -> flags = flags or readInputFlag()
                else -> {}
            }
        }
        return flags
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputFlag(): Int {
        parser.require(XmlPullParser.START_TAG, null, INPUT_KEY_FLAG_TAG)
        val inputKeyString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_KEY_FLAG_TAG)
        return Integer.valueOf(inputKeyString)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputKey(): Int {
        parser.require(XmlPullParser.START_TAG, null, INPUT_KEY_TAG)
        val inputKeyString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_KEY_TAG)

        //Strictly the inputKey has to has to be a Keycode from the KeyEvent class
        //Or it needs to be one of the customKeyCodes
        var keyCode = KeyEvent.keyCodeFromString(inputKeyString)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            keyCode = try {
                CustomKeycode.valueOf(inputKeyString).getKeyCode()
            } catch (error: IllegalArgumentException) {
                KeyEvent.KEYCODE_UNKNOWN
            }
        }
        return keyCode
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputString(): String {
        parser.require(XmlPullParser.START_TAG, null, INPUT_STRING_TAG)
        val inputString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_STRING_TAG)
        return inputString
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputCapsLockString(): String {
        parser.require(XmlPullParser.START_TAG, null, INPUT_CAPSLOCK_STRING_TAG)
        val inputString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_CAPSLOCK_STRING_TAG)
        return inputString
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMovementSequence(): MutableList<FingerPosition> {
        parser.require(XmlPullParser.START_TAG, null, MOVEMENT_SEQUENCE_TAG)
        val movementSequenceString = readText()
        parser.require(XmlPullParser.END_TAG, null, MOVEMENT_SEQUENCE_TAG)
        val movementSequenceList: Array<String> = movementSequenceString.split("\\s*;\\s*").toTypedArray()
        val movementSequence: MutableList<FingerPosition> = ArrayList()
        for (movement in movementSequenceList) {
            movementSequence.add(FingerPosition.valueOf(movement))
        }
        return movementSequence
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readKeyboardActionType(): KeyboardActionType {
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_ACTION_TYPE_TAG)
        val keyboardActionTypeString = readText()
        parser.require(XmlPullParser.END_TAG, null, KEYBOARD_ACTION_TYPE_TAG)
        return KeyboardActionType.valueOf(keyboardActionTypeString)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    companion object {
        private const val KEYBOARD_DATA_TAG: String = "keyboardData"
        private const val KEYBOARD_ACTION_MAP_TAG: String = "keyboardActionMap"
        private const val KEYBOARD_ACTION_TAG: String = "keyboardAction"
        private const val KEYBOARD_ACTION_TYPE_TAG: String = "keyboardActionType"
        private const val MOVEMENT_SEQUENCE_TAG: String = "movementSequence"
        private const val INPUT_STRING_TAG: String = "inputString"
        private const val INPUT_CAPSLOCK_STRING_TAG: String = "inputCapsLockString"
        private const val INPUT_KEY_TAG: String = "inputKey"
        private const val INPUT_KEY_FLAGS_TAG: String = "flags"
        private const val INPUT_KEY_FLAG_TAG: String = "flag"
        private const val KEYBOARD_CHARACTER_SET_TAG: String = "keyboardCharacterSet"
        private const val KEYBOARD_CHARACTER_SET_LOWERCASE_TAG: String = "lowerCase"
        private const val KEYBOARD_CHARACTER_SET_UPPERCASE_TAG: String = "upperCase"
    }

    init {
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
    }
}