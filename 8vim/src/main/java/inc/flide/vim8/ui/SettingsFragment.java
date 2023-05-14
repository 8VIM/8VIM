package inc.flide.vim8.ui;


import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.keyboardhelpers.KeyboardDataYamlParser;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.LayoutFileName;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String[] LAYOUT_FILTER = {"*/*"};
    private Context context;
    private final ActivityResultLauncher<String[]> openContent =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), selectedCustomLayoutFile -> {
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
                sharedPreferencesEditor.putString(getString(R.string.pref_selected_custom_keyboard_layout_uri),
                        selectedCustomLayoutFile.toString());
                sharedPreferencesEditor.apply();
                MainKeypadActionListener.rebuildKeyboardData(getResources(), context, selectedCustomLayoutFile);

            });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getContext();
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupPreferenceButtonActions();
    }

    private void setupPreferenceButtonActions() {
        setupEmojiKeyboardPreferenceAction();
        setupLayoutPreferenceAction();
        setupLoadCustomLayoutPreferenceAction();
    }

    private void setupLoadCustomLayoutPreferenceAction() {
        Preference loadCustomKeyboardPreference =
                findPreference(getString(R.string.pref_select_custom_keyboard_layout_key));
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

    private void askUserPreferredKeyboardLayout() {
        Map<String, String> inputMethodsNameAndId = findAllAvailableLayouts();

        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedKeyboardId =
                SharedPreferenceHelper.getInstance(context.getApplicationContext())
                        .getString(getString(R.string.pref_selected_keyboard_layout), "");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_keyboard_layout)).apply();
            }
        }
        createItemsChoice(R.string.select_preferred_keyboard_layout_dialog_title, inputMethodsNameAndId.keySet(),
                selectedKeyboardIndex,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_keyboard_layout),
                                keyboardIds.get(which));
                        sharedPreferencesEditor.putBoolean(getString(R.string.pref_use_custom_selected_keyboard_layout),
                                false);
                        sharedPreferencesEditor.apply();
                        MainKeypadActionListener.rebuildKeyboardData(getResources(), getContext());
                    }
                }).show();
    }

    private Map<String, String> findAllAvailableLayouts() {
        Map<String, String> languagesAndLayouts = new TreeMap<>();
        Resources resources = getResources();
        Context applicationContext = context.getApplicationContext();
        String[] fields = resources.getStringArray(R.array.keyboard_layouts_id);

        for (String field : fields) {
            LayoutFileName file = new LayoutFileName(resources, applicationContext, field);
            if (file.isValidLayout()) {
                languagesAndLayouts.put(file.getLayoutDisplayName(), file.getResourceName());
            }
        }

        return languagesAndLayouts;
    }

    private void askUserPreferredEmoticonKeyboard() {
        InputMethodManager imeManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        Map<String, String> inputMethodsNameAndId = new HashMap<>();
        for (InputMethodInfo inputMethodInfo : inputMethods) {
            if (inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) != 0) {
                inputMethodsNameAndId.put(inputMethodInfo.loadLabel(context.getPackageManager()).toString(),
                        inputMethodInfo.getId());
            }
        }
        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedKeyboardId =
                SharedPreferenceHelper.getInstance(context.getApplicationContext())
                        .getString(getString(R.string.pref_selected_emoticon_keyboard), "");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(getString(R.string.pref_selected_emoticon_keyboard)).apply();
            }
        }
        createItemsChoice(R.string.select_preferred_emoticon_keyboard_dialog_title, inputMethodsNameAndId.keySet(),
                selectedKeyboardIndex,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_emoticon_keyboard),
                                keyboardIds.get(which));
                        sharedPreferencesEditor.apply();
                    }
                }).show();
    }

    private MaterialDialog createItemsChoice(int titleRes, Collection<String> items, int selectedIndex,
                                             OnSelectCallback onSelectCallback) {
        return DialogSingleChoiceExtKt.listItemsSingleChoice(
                new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR()).title(titleRes, null)
                        .positiveButton(R.string.generic_okay_text, null, null)
                        .negativeButton(R.string.generic_cancel_text, null, null), null,
                new ArrayList<>(items), null, selectedIndex, true, -1, -1, (dialog, which, text) -> {
                    onSelectCallback.onSelect(dialog, which, text);
                    return null;
                });
    }

    private interface OnSelectCallback {
        void onSelect(MaterialDialog dialog, int index, CharSequence test);
    }
}
