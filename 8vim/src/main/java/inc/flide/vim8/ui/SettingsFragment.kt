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
import inc.flide.vim8.views.mainKeyboard.MainKeyboardView
import inc.flide.vim8.views.NumberKeypadView
import inc.flide.vim8.views.SelectionKeypadView
import inc.flide.vim8.views.SymbolKeypadView
import android.os.IBinder
import android.text.TextUtils
import android.app.Application
import android.content.Context
import android.view.*
import android.view.inputmethod.*
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import inc.flide.vim8.structures.*
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        setupPreferenceButtonActions()
    }

    private fun setupPreferenceButtonActions() {
        setupEmojiKeyboardPreferenceAction()
        setupLayoutPreferenceAction()
        setupLoadCustomLayoutPreferenceAction()
    }

    private fun setupLoadCustomLayoutPreferenceAction() {
        val loadCustomKeyboardPreference = findPreference<Preference?>(getString(R.string.pref_select_custom_keyboard_layout_key))!!
        loadCustomKeyboardPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            askUserLoadCustomKeyboardLayout()
            true
        }
    }

    private fun askUserLoadCustomKeyboardLayout() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/xml")
        startActivityForResult(Intent.createChooser(intent, "Select a layout file"), PICK_KEYBOARD_LAYOUT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val context = context
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val sharedPreferencesEditor = sharedPreferences.edit()
        if (requestCode == PICK_KEYBOARD_LAYOUT_FILE && resultCode == Activity.RESULT_OK) {
            // TODO: Verify if the picked file is actually a valid layout file.
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val selectedCustomLayoutFile = data.getData()
            getContext().getContentResolver().takePersistableUriPermission(selectedCustomLayoutFile, takeFlags)
            sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), true)
            sharedPreferencesEditor.putString(getString(R.string.pref_selected_custom_keyboard_layout_uri), selectedCustomLayoutFile.toString())
            sharedPreferencesEditor.apply()
            MainKeypadActionListener.Companion.rebuildKeyboardData(resources, getContext(), selectedCustomLayoutFile)
        }
    }

    private fun setupLayoutPreferenceAction() {
        val keyboardPref = findPreference<Preference?>(getString(R.string.pref_select_keyboard_layout_key))!!
        keyboardPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            askUserPreferredKeyboardLayout()
            true
        }
    }

    private fun setupEmojiKeyboardPreferenceAction() {
        val emojiKeyboardPref = findPreference<Preference?>(getString(R.string.pref_select_emoji_keyboard_key))!!
        emojiKeyboardPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            askUserPreferredEmoticonKeyboard()
            true
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        Toast.makeText(context, "test" + newValue.toString(), Toast.LENGTH_LONG).show()
        if (preference is SeekBarPreference) {
            Toast.makeText(context, "test" + newValue.toString(), Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun askUserPreferredKeyboardLayout() {
        val context = context
        val inputMethodsNameAndId = findAllAvailableLayouts()
        val keyboardIds = ArrayList(inputMethodsNameAndId.values)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val selectedKeyboardId: String = SharedPreferenceHelper.Companion.getInstance(context.getApplicationContext())
                .getString(
                        getString(R.string.pref_selected_keyboard_layout),
                        "")
        var selectedKeyboardIndex = -1
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId)
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_keyboard_layout)).apply()
            }
        }
        MaterialDialog.Builder(context)
                .title(R.string.select_preferred_keyboard_layout_dialog_title)
                .items(inputMethodsNameAndId.keys)
                .itemsCallbackSingleChoice(selectedKeyboardIndex) { dialog: MaterialDialog?, itemView: View?, which: Int, text: CharSequence? ->
                    if (which != -1) {
                        val sharedPreferencesEditor = sharedPreferences.edit()
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_keyboard_layout), keyboardIds[which])
                        sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), false)
                        sharedPreferencesEditor.apply()
                        MainKeypadActionListener.Companion.rebuildKeyboardData(resources, getContext())
                    }
                    true
                }
                .positiveText(R.string.generic_okay_text)
                .show()
    }

    private fun findAllAvailableLayouts(): MutableMap<String?, String?>? {
        val languagesAndLayouts: MutableMap<String?, String?> = TreeMap()
        val fields = raw::class.java.fields
        for (count in fields.indices) {
            val file = LayoutFileName(fields[count].name)
            if (file.isValidLayout) {
                languagesAndLayouts[file.layoutDisplayName] = file.resourceName
            }
        }
        return languagesAndLayouts
    }

    private fun askUserPreferredEmoticonKeyboard() {
        val context = context
        val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val inputMethods = imeManager.enabledInputMethodList
        val inputMethodsNameAndId: MutableMap<String?, String?> = HashMap()
        for (inputMethodInfo in inputMethods) {
            if (inputMethodInfo.id.compareTo(Constants.SELF_KEYBOARD_ID) != 0) {
                inputMethodsNameAndId[inputMethodInfo.loadLabel(context.getPackageManager()).toString()] = inputMethodInfo.id
            }
        }
        val keyboardIds = ArrayList(inputMethodsNameAndId.values)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val selectedKeyboardId: String = SharedPreferenceHelper.Companion.getInstance(context.getApplicationContext())
                .getString(
                        getString(R.string.pref_selected_emoticon_keyboard),
                        "")
        var selectedKeyboardIndex = -1
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId)
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_emoticon_keyboard)).apply()
            }
        }
        MaterialDialog.Builder(context)
                .title(R.string.select_preferred_emoticon_keyboard_dialog_title)
                .items(inputMethodsNameAndId.keys)
                .itemsCallbackSingleChoice(selectedKeyboardIndex) { dialog: MaterialDialog?, itemView: View?, which: Int, text: CharSequence? ->
                    if (which != -1) {
                        val sharedPreferencesEditor = sharedPreferences.edit()
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_emoticon_keyboard), keyboardIds[which])
                        sharedPreferencesEditor.apply()
                    }
                    true
                }
                .positiveText(R.string.generic_okay_text)
                .show()
    }

    companion object {
        private const val PICK_KEYBOARD_LAYOUT_FILE = 1
    }
}