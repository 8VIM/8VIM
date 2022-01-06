package inc.flide.vim8.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.afollestad.materialdialogs.MaterialDialog
import inc.flide.vim8.R
import inc.flide.vim8.R.raw
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.Constants
import inc.flide.vim8.structures.LayoutFileName
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