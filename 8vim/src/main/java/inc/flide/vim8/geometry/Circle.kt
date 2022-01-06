package inc.flide.vim8.geometry

import androidx.constraintlayout.widget.ConstraintLayout
import android.os.Bundle
import inc.flide.vim8.R
import android.view.View.OnTouchListener
import android.view.MotionEvent
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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Gravity
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
import android.view.HapticFeedbackConstants
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

class Circle {
    private var centre: PointF?
    private var radius: Float

    constructor() {
        centre = PointF(0f, 0f)
        radius = 0f
    }

    constructor(centre: PointF?, radius: Float) {
        this.centre = centre
        this.radius = radius
    }

    fun getCentre(): PointF? {
        return centre
    }

    fun setCentre(centre: PointF?) {
        this.centre = centre
    }

    fun getRadius(): Float {
        return radius
    }

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    private fun getPowerOfPoint(point: PointF?): Double {
        /*
        If O is the centre of circle
        Consider startingPoint point P not necessarily on the circumference of the circle.
        If d = OP is the distance between P and the circle's center O, then the power of the point P relative to the circle is
        p=d^2-r^2.
        */
        val dSquare = GeometricUtilities.getSquaredDistanceBetweenPoints(point, centre)
        val rSquare = Math.pow(radius.toDouble(), 2.0)
        return dSquare - rSquare
    }

    fun isPointInsideCircle(point: PointF?): Boolean {
        return getPowerOfPoint(point) < 0
    }

    /**
     * Gets the angle of point p relative to the center
     */
    private fun getAngleInRadiansOfPointWithRespectToCentreOfCircle(point: PointF?): Double {
        // Get difference of coordinates
        val x = (point.x - centre.x).toDouble()
        val y = (centre.y - point.y).toDouble()

        // Calculate angle with special atan (calculates the correct angle in all quadrants)
        var angle = Math.atan2(y, x)
        // Make all angles positive
        if (angle < 0) {
            angle = Math.PI * 2 + angle
        }
        return angle
    }

    /**
     * Get the number of the sector that point p is in
     *
     * @return 0: right, 1: top, 2: left, 3: bottom
     */
    fun getSectorOfPoint(p: PointF?): FingerPosition? {
        val angleDouble = getAngleInRadiansOfPointWithRespectToCentreOfCircle(p)
        val angleToSectorValue = angleDouble / (Math.PI / 2)
        val quadrantCyclic = Math.round(angleToSectorValue) as Int
        val baseQuadrant = GeometricUtilities.getBaseQuadrant(quadrantCyclic)
        when (baseQuadrant) {
            0 -> return FingerPosition.RIGHT
            1 -> return FingerPosition.TOP
            2 -> return FingerPosition.LEFT
            3 -> return FingerPosition.BOTTOM
        }
        return null
    }
}