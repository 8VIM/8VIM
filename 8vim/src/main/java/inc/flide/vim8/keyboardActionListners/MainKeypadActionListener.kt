package inc.flide.vim8.keyboardActionListners

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
import android.graphics.PointF
import inc.flide.vim8.geometry.Circle
import android.graphics.RectF
import android.graphics.PathMeasure
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
import android.graphics.Typeface
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import android.widget.ImageButton
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
import inc.flide.vim8.keyboardHelpers.KeyboardDataXmlParser
import android.util.Xml
import inc.flide.vim8.keyboardHelpers.InputMethodServiceHelper
import android.media.AudioManager
import inc.flide.vim8.keyboardActionListners.KeypadActionListener
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
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Handler
import android.view.*
import inc.flide.vim8.structures.*
import java.util.ArrayList

class MainKeypadActionListener(inputMethodService: MainInputMethodService?, view: View?) : KeypadActionListener(inputMethodService, view) {
    private val longPressHandler: Handler? = Handler()
    private val movementSequence: MutableList<FingerPosition?>?
    private var currentFingerPosition: FingerPosition?
    private var isLongPressCallbackSet = false
    private var currentMovementSequenceType: MovementSequenceType? = MovementSequenceType.NO_MOVEMENT
    private val longPressRunnable: Runnable? = object : Runnable {
        override fun run() {
            val movementSequenceAgumented: MutableList<FingerPosition?> = ArrayList(movementSequence)
            movementSequenceAgumented.add(FingerPosition.LONG_PRESS)
            processMovementSequence(movementSequenceAgumented)
            longPressHandler.postDelayed(this, Constants.DELAY_MILLIS_LONG_PRESS_CONTINUATION.toLong())
        }
    }

    fun getLowerCaseCharacters(): String? {
        return keyboardData.getLowerCaseCharacters()
    }

    fun getUpperCaseCharacters(): String? {
        return keyboardData.getUpperCaseCharacters()
    }

    fun movementStarted(fingerPosition: FingerPosition?) {
        currentFingerPosition = fingerPosition
        movementSequence.clear()
        currentMovementSequenceType = MovementSequenceType.NEW_MOVEMENT
        movementSequence.add(currentFingerPosition)
        initiateLongPressDetection()
    }

    fun movementContinues(fingerPosition: FingerPosition?) {
        val lastKnownFingerPosition = currentFingerPosition
        currentFingerPosition = fingerPosition
        val isFingerPositionChanged = lastKnownFingerPosition != currentFingerPosition
        if (isFingerPositionChanged) {
            interruptLongPress()
            movementSequence.add(currentFingerPosition)
            if (currentFingerPosition == FingerPosition.INSIDE_CIRCLE
                    && keyboardData.getActionMap()[movementSequence] != null) {
                processMovementSequence(movementSequence)
                movementSequence.clear()
                currentMovementSequenceType = MovementSequenceType.CONTINUED_MOVEMENT
                movementSequence.add(currentFingerPosition)
            }
        } else if (!isLongPressCallbackSet) {
            initiateLongPressDetection()
        }
    }

    fun movementEnds() {
        interruptLongPress()
        currentFingerPosition = FingerPosition.NO_TOUCH
        movementSequence.add(currentFingerPosition)
        processMovementSequence(movementSequence)
        movementSequence.clear()
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    fun movementCanceled() {
        longPressHandler.removeCallbacks(longPressRunnable)
        isLongPressCallbackSet = false
        movementSequence.clear()
        currentMovementSequenceType = MovementSequenceType.NO_MOVEMENT
    }

    private fun initiateLongPressDetection() {
        isLongPressCallbackSet = true
        longPressHandler.postDelayed(longPressRunnable, Constants.DELAY_MILLIS_LONG_PRESS_INITIATION.toLong())
    }

    private fun interruptLongPress() {
        longPressHandler.removeCallbacks(longPressRunnable)
        val movementSequenceAgumented: MutableList<FingerPosition?> = ArrayList(movementSequence)
        movementSequenceAgumented.add(FingerPosition.LONG_PRESS_END)
        processMovementSequence(movementSequenceAgumented)
        isLongPressCallbackSet = false
    }

    private fun processMovementSequence(movementSequence: MutableList<FingerPosition?>?) {
        var keyboardAction = keyboardData.getActionMap()[movementSequence]
        if (keyboardAction == null && currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT) {
            val modifiedMovementSequence: MutableList<FingerPosition?> = ArrayList(movementSequence)
            modifiedMovementSequence.add(0, FingerPosition.NO_TOUCH)
            keyboardAction = keyboardData.getActionMap()[modifiedMovementSequence]
        }
        if (keyboardAction == null) {
            movementSequence.clear()
            return
        }
        when (keyboardAction.keyboardActionType) {
            KeyboardActionType.INPUT_TEXT -> handleInputText(keyboardAction)
            KeyboardActionType.INPUT_KEY -> handleInputKey(keyboardAction)
        }
    }

    companion object {
        private var keyboardData: KeyboardData?
        fun rebuildKeyboardData(resource: Resources?, context: Context?) {
            keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resource, context)
        }

        fun rebuildKeyboardData(resource: Resources?, context: Context?, customLayoutUri: Uri?) {
            keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resource, context, customLayoutUri)
        }
    }

    init {
        keyboardData = mainInputMethodService.buildKeyboardActionMap()
        movementSequence = ArrayList()
        currentFingerPosition = FingerPosition.NO_TOUCH
    }
}