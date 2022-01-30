package inc.flide.vim8.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.LayoutFileName;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);

        setupPreferenceButtonActions();
    }

    private void setupPreferenceButtonActions() {
        setupEmojiKeyboardPreferenceAction();
        setupLayoutPreferenceAction();
        setupLoadCustomLayoutPreferenceAction();
    }

    private void setupLoadCustomLayoutPreferenceAction() {
        Preference loadCustomKeyboardPreference = findPreference(getString(R.string.pref_select_custom_keyboard_layout_key));
        assert loadCustomKeyboardPreference != null;

        loadCustomKeyboardPreference.setOnPreferenceClickListener(preference -> {
            askUserLoadCustomKeyboardLayout();
            return true;
        });
    }

    private static final int PICK_KEYBOARD_LAYOUT_FILE = 1;
    private void askUserLoadCustomKeyboardLayout() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/xml");
        startActivityForResult(Intent.createChooser(intent, "Select a layout file"), PICK_KEYBOARD_LAYOUT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();


        if (requestCode == PICK_KEYBOARD_LAYOUT_FILE && resultCode == RESULT_OK) {
            // TODO: Verify if the picked file is actually a valid layout file.
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            Uri selectedCustomLayoutFile = data.getData();
            getContext().getContentResolver().takePersistableUriPermission(selectedCustomLayoutFile, takeFlags);
            sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), true);
            sharedPreferencesEditor.putString(getString(R.string.pref_selected_custom_keyboard_layout_uri), selectedCustomLayoutFile.toString());
            sharedPreferencesEditor.apply();
            MainKeypadActionListener.rebuildKeyboardData(getResources(), getContext(), selectedCustomLayoutFile);

        }
    }
    private void setupLayoutPreferenceAction() {
        Preference keyboardPref = findPreference(getString(R.string.pref_select_keyboard_layout_key));
        assert keyboardPref != null;

        keyboardPref.setOnPreferenceClickListener(preference -> {
            askUserPreferredKeyboardLayout();
            return true;
        });
    }

    private void setupEmojiKeyboardPreferenceAction() {
        Preference emojiKeyboardPref = findPreference(getString(R.string.pref_select_emoji_keyboard_key));
        assert emojiKeyboardPref != null;

        emojiKeyboardPref.setOnPreferenceClickListener(preference -> {
            askUserPreferredEmoticonKeyboard();
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Toast.makeText(getContext(), "test" + newValue.toString(), Toast.LENGTH_LONG).show();
        if (preference instanceof SeekBarPreference) {
            Toast.makeText(getContext(), "test" + newValue.toString(), Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void askUserPreferredKeyboardLayout() {
        Context context = getContext();

        Map<String, String> inputMethodsNameAndId = findAllAvailableLayouts();

        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedKeyboardId = SharedPreferenceHelper
                .getInstance(context.getApplicationContext())
                .getString(
                        getString(R.string.pref_selected_keyboard_layout),
                        "");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_keyboard_layout)).apply();
            }
        }
        new MaterialDialog.Builder(context)
                .title(R.string.select_preferred_keyboard_layout_dialog_title)
                .items(inputMethodsNameAndId.keySet())
                .itemsCallbackSingleChoice(selectedKeyboardIndex, (dialog, itemView, which, text) -> {

                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_keyboard_layout), keyboardIds.get(which));
                        sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), false);
                        sharedPreferencesEditor.apply();
                        MainKeypadActionListener.rebuildKeyboardData(getResources(), getContext());
                    }
                    return true;
                })
                .positiveText(R.string.generic_okay_text)
                .show();
    }

    private Map<String, String> findAllAvailableLayouts() {
        Map<String, String> languagesAndLayouts = new TreeMap<>();
        String[] fields = getResources().getStringArray(R.array.keyboard_layouts_id);
        for (int count = 0; count < fields.length; count++) {
            LayoutFileName file = new LayoutFileName(fields[count]);
            if (file.isValidLayout()) {
                languagesAndLayouts.put(file.getLayoutDisplayName(), file.getResourceName());
            }
        }

        return languagesAndLayouts;
    }

    private void askUserPreferredEmoticonKeyboard() {
        Context context = getContext();
        InputMethodManager imeManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        Map<String, String> inputMethodsNameAndId = new HashMap<>();
        for (InputMethodInfo inputMethodInfo : inputMethods) {
            if (inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) != 0) {
                inputMethodsNameAndId.put(inputMethodInfo.loadLabel(context.getPackageManager()).toString(), inputMethodInfo.getId());
            }
        }
        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedKeyboardId = SharedPreferenceHelper
                .getInstance(context.getApplicationContext())
                .getString(
                        getString(R.string.pref_selected_emoticon_keyboard),
                        "");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_emoticon_keyboard)).apply();
            }
        }
        new MaterialDialog.Builder(context)
                .title(R.string.select_preferred_emoticon_keyboard_dialog_title)
                .items(inputMethodsNameAndId.keySet())
                .itemsCallbackSingleChoice(selectedKeyboardIndex, (dialog, itemView, which, text) -> {

                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_emoticon_keyboard), keyboardIds.get(which));
                        sharedPreferencesEditor.apply();
                    }
                    return true;
                })
                .positiveText(R.string.generic_okay_text)
                .show();
    }
}
