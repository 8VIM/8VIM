package inc.flide.vim8.ui;


import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener;
import inc.flide.vim8.keyboardHelpers.KeyboardDataYamlParser;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.LayoutFileName;

public class SettingsFragment extends PreferenceFragmentCompat
    implements Preference.OnPreferenceChangeListener {
    private static final String[] LAYOUT_FILTER = {"*/*"};
    private final ActivityResultLauncher<String[]> openContent =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), selectedCustomLayoutFile -> {
            Context context = getContext();
            if (selectedCustomLayoutFile == null || context == null) {
                return;
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(selectedCustomLayoutFile, takeFlags);
            try (InputStream inputStream = context.getContentResolver().openInputStream(selectedCustomLayoutFile)) {
                if (KeyboardDataYamlParser.isValidFile(inputStream) == 0) {
                    return;
                }
            } catch (Exception e) {
                return;
            }

            sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), true);
            sharedPreferencesEditor.putString(getString(R.string.pref_selected_custom_keyboard_layout_uri), selectedCustomLayoutFile.toString());
            sharedPreferencesEditor.apply();
            MainKeypadActionListener.rebuildKeyboardData(getResources(), context, selectedCustomLayoutFile);

        });

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


    private void askUserLoadCustomKeyboardLayout() {
        openContent.launch(LAYOUT_FILTER);
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
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        Toast.makeText(getContext(), "test" + newValue.toString(), Toast.LENGTH_LONG).show();
        if (preference instanceof SeekBarPreference) {
            Toast.makeText(getContext(), "test" + newValue, Toast.LENGTH_LONG).show();
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
        DialogSingleChoiceExtKt.listItemsSingleChoice(
                new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR())
                    .title(R.string.select_preferred_keyboard_layout_dialog_title, null)
                    .positiveButton(R.string.generic_okay_text, null, null),
                null,
                new ArrayList<>(inputMethodsNameAndId.keySet()),
                null,
                selectedKeyboardIndex,
                true,
                -1,
                -1,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_keyboard_layout), keyboardIds.get(which));
                        sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout), false);
                        sharedPreferencesEditor.apply();
                        MainKeypadActionListener.rebuildKeyboardData(getResources(), getContext());
                    }
                    return null;
                }
            )
            .show();
    }

    private Map<String, String> findAllAvailableLayouts() {
        Map<String, String> languagesAndLayouts = new TreeMap<>();
        Resources resources = getResources();
        Context context = getContext().getApplicationContext();
        String[] fields = resources.getStringArray(R.array.keyboard_layouts_id);

        for (String field : fields) {
            LayoutFileName file = new LayoutFileName(resources, context, field);
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
        DialogSingleChoiceExtKt.listItemsSingleChoice(
                new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR())
                    .title(R.string.select_preferred_emoticon_keyboard_dialog_title, null)
                    .positiveButton(R.string.generic_okay_text, null, null),
                null,
                new ArrayList<>(inputMethodsNameAndId.keySet()),
                null,
                selectedKeyboardIndex,
                true,
                -1,
                -1,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_emoticon_keyboard), keyboardIds.get(which));
                        sharedPreferencesEditor.apply();
                    }
                    return null;
                }
            )
            .show();
    }
}
