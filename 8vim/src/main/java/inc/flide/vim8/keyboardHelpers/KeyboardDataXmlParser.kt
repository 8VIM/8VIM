package inc.flide.vim8.keyboardHelpers

import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Bundle
import inc.flide.vim8.R
import android.view.View.OnTouchListener
import android.content.Intent
import inc.flide.vim8.ui.SettingsActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import inc.flide.vim8.ui.SettingsFragment
import android.widget.Toast
import inc.flide.vim8.ui.AboutUsActivity
import androidx.core.view.GravityCompat
import android.view.inputmethod.InputMethodInfo
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.afollestad.materialdialogs.DialogAction
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences
import android.app.Activity
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import androidx.preference.SeekBarPreference
import inc.flide.vim8.preferences.SharedPreferenceHelper
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
import inc.flide.vim8.R.raw
import inc.flide.vim8.structures.LayoutFileName
import android.graphics.PointF
import inc.flide.vim8.geometry.Circle
import android.graphics.RectF
import android.graphics.PathMeasure
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
import android.graphics.Typeface
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import inc.flide.vim8.structures.FingerPosition
import android.widget.ImageButton
import inc.flide.vim8.structures.KeyboardAction
import inc.flide.vim8.structures.KeyboardActionType
import inc.flide.vim8.structures.CustomKeycode
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.Keyboard
import inc.flide.vim8.views.ButtonKeypadView
import inc.flide.vim8.keyboardActionListners.ButtonKeypadActionListener
import inc.flide.vim8.geometry.GeometricUtilities
import kotlin.jvm.JvmOverloads
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import org.xmlpull.v1.XmlPullParser
import kotlin.Throws
import org.xmlpull.v1.XmlPullParserException
import inc.flide.vim8.structures.KeyboardData
import inc.flide.vim8.keyboardHelpers.KeyboardDataXmlParser
import android.util.Xml
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper
import android.media.AudioManager
import inc.flide.vim8.keyboardActionListners.KeypadActionListener
import inc.flide.vim8.structures.MovementSequenceType
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import android.os.IBinder
import android.text.TextUtils
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.app.Application
import android.view.*
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.AbstractMap
import java.util.ArrayList
import java.util.HashMap

internal class KeyboardDataXmlParser(inputStream: InputStream?) {
    private val parser: XmlPullParser?
    @Throws(IOException::class, XmlPullParserException::class)
    fun readKeyboardData(): KeyboardData? {
        val keyboardData = KeyboardData()
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_DATA_TAG)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.getName()
            when (tagName) {
                KEYBOARD_CHARACTER_SET_TAG -> readKeyboardCharacterSet(keyboardData)
                KEYBOARD_ACTION_MAP_TAG -> keyboardData.actionMap = readKeyboardActionMap()
            }
        }
        return keyboardData
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readKeyboardActionMap(): MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>? {
        val keyboardActionMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?> = HashMap()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.getName()
            if (tagName == KEYBOARD_ACTION_TAG) {
                val keyboardAction = readKeyboardAction()
                keyboardActionMap[keyboardAction.key] = keyboardAction.value
            }
        }
        return keyboardActionMap
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readKeyboardCharacterSet(keyboardData: KeyboardData?) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.getName()
            when (tagName) {
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
    private fun readKeyboardAction(): MutableMap.MutableEntry<MutableList<FingerPosition?>?, KeyboardAction?>? {
        var movementSequence: MutableList<FingerPosition?>? = null
        val keyboardAction: KeyboardAction
        var keyboardActionType: KeyboardActionType? = null
        var associatedText: String? = ""
        var associatedCapsLockText: String? = ""
        var keyEventCode = 0
        var flags = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.getName()
            when (tagName) {
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
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.getName()
            when (tagName) {
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
                CustomKeycode.valueOf(inputKeyString).keyCode
            } catch (error: IllegalArgumentException) {
                KeyEvent.KEYCODE_UNKNOWN
            }
        }
        return keyCode
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputString(): String? {
        parser.require(XmlPullParser.START_TAG, null, INPUT_STRING_TAG)
        val inputString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_STRING_TAG)
        return inputString
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readInputCapsLockString(): String? {
        parser.require(XmlPullParser.START_TAG, null, INPUT_CAPSLOCK_STRING_TAG)
        val inputString = readText()
        parser.require(XmlPullParser.END_TAG, null, INPUT_CAPSLOCK_STRING_TAG)
        return inputString
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMovementSequence(): MutableList<FingerPosition?>? {
        parser.require(XmlPullParser.START_TAG, null, MOVEMENT_SEQUENCE_TAG)
        val movementSequenceString = readText()
        parser.require(XmlPullParser.END_TAG, null, MOVEMENT_SEQUENCE_TAG)
        val movementSequenceList: Array<String?> = movementSequenceString.split("\\s*;\\s*").toTypedArray()
        val movementSequence: MutableList<FingerPosition?> = ArrayList()
        for (movement in movementSequenceList) {
            movementSequence.add(FingerPosition.valueOf(movement))
        }
        return movementSequence
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readKeyboardActionType(): KeyboardActionType? {
        parser.require(XmlPullParser.START_TAG, null, KEYBOARD_ACTION_TYPE_TAG)
        val keyboardActionTypeString = readText()
        parser.require(XmlPullParser.END_TAG, null, KEYBOARD_ACTION_TYPE_TAG)
        return KeyboardActionType.valueOf(keyboardActionTypeString)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(): String? {
        var result: String? = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText()
            parser.nextTag()
        }
        return result
    }

    companion object {
        private val KEYBOARD_DATA_TAG: String? = "keyboardData"
        private val KEYBOARD_ACTION_MAP_TAG: String? = "keyboardActionMap"
        private val KEYBOARD_ACTION_TAG: String? = "keyboardAction"
        private val KEYBOARD_ACTION_TYPE_TAG: String? = "keyboardActionType"
        private val MOVEMENT_SEQUENCE_TAG: String? = "movementSequence"
        private val INPUT_STRING_TAG: String? = "inputString"
        private val INPUT_CAPSLOCK_STRING_TAG: String? = "inputCapsLockString"
        private val INPUT_KEY_TAG: String? = "inputKey"
        private val INPUT_KEY_FLAGS_TAG: String? = "flags"
        private val INPUT_KEY_FLAG_TAG: String? = "flag"
        private val KEYBOARD_CHARACTER_SET_TAG: String? = "keyboardCharacterSet"
        private val KEYBOARD_CHARACTER_SET_LOWERCASE_TAG: String? = "lowerCase"
        private val KEYBOARD_CHARACTER_SET_UPPERCASE_TAG: String? = "upperCase"
    }

    init {
        parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
    }
}