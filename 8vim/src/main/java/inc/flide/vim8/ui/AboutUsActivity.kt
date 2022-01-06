package inc.flide.vim8.ui

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
import inc.flide.vim8.geometry.Circle
import inc.flide.vim8.MainInputMethodService
import android.view.View.MeasureSpec
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
import android.graphics.*
import android.net.Uri
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AboutUsActivity : AppCompatActivity() {
    private var github: ConstraintLayout? = null
    private var matrix: ConstraintLayout? = null
    private var website: ConstraintLayout? = null
    private var twitter: ConstraintLayout? = null
    private var googlePlayStore: ConstraintLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us_activity_layout)
        github = findViewById(R.id.constraintLayout_github)
        github.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> github.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    github.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://github.com/flide/8vim")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        matrix = findViewById(R.id.constraintLayout_matrix)
        matrix.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> matrix.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    matrix.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://app.element.io/#/room/#8vim/lobby:matrix.org")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        website = findViewById(R.id.constraintLayout_website)
        website.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> website.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    website.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://8vim.com")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        twitter = findViewById(R.id.constraintLayout_twitter)
        twitter.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> twitter.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    twitter.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://twitter.com/8vim_?s=09")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        googlePlayStore = findViewById(R.id.constraintLayout_playstore)
        googlePlayStore.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> googlePlayStore.setBackgroundColor(Color.LTGRAY)
                MotionEvent.ACTION_UP -> {
                    googlePlayStore.setBackgroundColor(Color.WHITE)
                    val uri = Uri.parse("https://play.google.com/store/apps/details?id=inc.flide.vi8") // missing 'http://' will cause crashed
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
            true
        })
        val backArrow = findViewById<ImageView?>(R.id.back_arrow)
        backArrow.setOnTouchListener { view: View?, motionEvent: MotionEvent? ->
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                backArrow.setBackgroundColor(Color.LTGRAY)
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                backArrow.setBackgroundColor(Color.WHITE)
                val intent = Intent(this@AboutUsActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
            true
        }
    }
}