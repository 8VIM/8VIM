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
import androidx.appcompat.app.AppCompatDelegate;
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
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.LayoutFileName;
import inc.flide.vim8.structures.exceptions.YamlException;
import inc.flide.vim8.utils.AlertHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String[] LAYOUT_FILTER = {"application/octet-stream"};
    private Context context;
    private KeyboardDataYamlParser keyboardDataYamlParser;
    private final ActivityResultLauncher<String[]> openContent =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    selectedCustomLayoutFile -> {
                        if (selectedCustomLayoutFile == null || context == null) {
                            return;
                        }
                        SharedPreferences sharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        context.getContentResolver()
                                .takePersistableUriPermission(selectedCustomLayoutFile, takeFlags);
                        try (InputStream inputStream = context.getContentResolver()
                                .openInputStream(selectedCustomLayoutFile)) {
                            KeyboardData keyboardData = keyboardDataYamlParser.readKeyboardData(inputStream);
                            if (keyboardData.getTotalLayers() == 0) {
                                AlertHelper.showAlert(context, R.string.yaml_error_title,
                                        "The layout requires at least one layer");
                                return;
                            }
                        } catch (YamlException e) {
                            AlertHelper.showAlert(context, R.string.yaml_error_title, e.getMessage());
                            return;
                        } catch (IOException e) {
                            AlertHelper.showAlert(context, R.string.generic_error_text, e.getMessage());
                            return;
                        }
                        sharedPreferencesEditor.putBoolean(
                                getString(R.string.pref_use_custom_selected_keyboard_layout), true);
                        sharedPreferencesEditor.putString(
                                getString(R.string.pref_selected_custom_keyboard_layout_uri),
                                selectedCustomLayoutFile.toString());
                        sharedPreferencesEditor.apply();
                        MainKeypadActionListener.rebuildKeyboardData(getResources(), context,
                                selectedCustomLayoutFile);
                    });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getContext();
        keyboardDataYamlParser = KeyboardDataYamlParser.getInstance();
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setupPreferenceButtonActions();
        setupPreferenceCallbacks();
    }

    private void setupPreferenceCallbacks() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefRandomTrailKey = getString(R.string.pref_random_trail_color_key);
        String prefColorModeKey = getString(R.string.pref_color_mode_key);
        Preference preferenceTrailColor = findPreference(getString(R.string.pref_trail_color_key));

        Preference randomTrailColorPreference = findPreference(prefRandomTrailKey);
        randomTrailColorPreference.setOnPreferenceChangeListener((pref, value) -> {
            preferenceTrailColor.setVisible(!((boolean) value));
            return true;
        });
        preferenceTrailColor.setVisible(
                !defaultSharedPreferences.getBoolean(prefRandomTrailKey, false));
        setColorsSelectionVisible(defaultSharedPreferences.getString(prefColorModeKey, "system"));

        findPreference(prefColorModeKey).setOnPreferenceChangeListener((pref, value) -> {
            switch ((String) value) {
                case "system":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                case "dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                default:
            }
            setColorsSelectionVisible((String) value);
            return true;
        });
    }

    private void setColorsSelectionVisible(String mode) {
        boolean visible = mode.equals("custom");
        findPreference(getString(R.string.pref_board_bg_color_key)).setVisible(visible);
        findPreference(getString(R.string.pref_board_fg_color_key)).setVisible(visible);
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
        createItemsChoice(R.string.select_preferred_keyboard_layout_dialog_title,
                inputMethodsNameAndId.keySet(),
                selectedKeyboardIndex,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_keyboard_layout),
                                keyboardIds.get(which));
                        sharedPreferencesEditor.putBoolean(
                                getString(R.string.pref_use_custom_selected_keyboard_layout),
                                false);
                        sharedPreferencesEditor.apply();
                        MainKeypadActionListener.rebuildKeyboardData(getResources(), getContext());
                    }
                    return null;
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
        InputMethodManager imeManager =
                (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
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
                sharedPreferences.edit().remove(getString(R.string.pref_selected_emoticon_keyboard))
                        .apply();
            }
        }
        createItemsChoice(R.string.select_preferred_emoticon_keyboard_dialog_title,
                inputMethodsNameAndId.keySet(),
                selectedKeyboardIndex,
                (dialog, which, text) -> {
                    if (which != -1) {
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.pref_selected_emoticon_keyboard),
                                keyboardIds.get(which));
                        sharedPreferencesEditor.apply();
                    }
                    return null;
                }).show();
    }

    private MaterialDialog createItemsChoice(int titleRes, Collection<String> items,
                                             int selectedIndex,
                                             Function3<MaterialDialog, Integer, CharSequence, Unit> callback) {
        return DialogSingleChoiceExtKt.listItemsSingleChoice(
                new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR()).title(titleRes, null)
                        .positiveButton(R.string.generic_okay_text, null, null)
                        .negativeButton(R.string.generic_cancel_text, null, null), null,
                new ArrayList<>(items), null, selectedIndex, true, -1, -1, callback);
    }
}
