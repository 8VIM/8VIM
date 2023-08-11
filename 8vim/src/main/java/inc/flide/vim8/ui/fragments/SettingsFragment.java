package inc.flide.vim8.ui.fragments;


import static android.content.Context.INPUT_METHOD_SERVICE;
import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import inc.flide.vim8.R;
import inc.flide.vim8.datastore.model.PreferenceData;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.AvailableLayouts;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.theme.ThemeMode;
import inc.flide.vim8.utils.DialogsHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends LayoutFileSelector {
    private Context context;
    private AvailableLayouts availableLayouts;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getContext();
        assert context != null;
        prefs = appPreferenceModel().java();
        availableLayouts = AvailableLayouts.getInstance();
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setupPreferenceButtonActions();
        setupPreferenceCallbacks();
    }

    private void setupPreferenceCallbacks() {
        AppPrefs.Keyboard.Trail trailPrefs = prefs.getKeyboard().getTrail();
        Preference preferenceTrailColor = findPreference(trailPrefs.getColor().getKey());
        Preference colorModePreference = findPreference(prefs.getTheme().getMode().getKey());
        Preference randomTrailColorPreference = findPreference(trailPrefs.getUseRandomColor().getKey());

        assert randomTrailColorPreference != null;
        assert preferenceTrailColor != null;
        assert colorModePreference != null;

        randomTrailColorPreference.setOnPreferenceChangeListener((pref, value) -> {
            preferenceTrailColor.setVisible(!((boolean) value));
            return true;
        });
        preferenceTrailColor.setVisible(!trailPrefs.getUseRandomColor().get());
        setColorsSelectionVisible(prefs.getTheme().getMode().get());

        colorModePreference.setOnPreferenceChangeListener((pref, value) -> {
            ThemeMode mode = ThemeMode.valueOf((String) value);
            switch (mode) {
                case DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                case LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            setColorsSelectionVisible(mode);
            return true;
        });
    }

    private void setColorsSelectionVisible(ThemeMode mode) {
        boolean visible = mode == ThemeMode.CUSTOM;
        AppPrefs.Keyboard.CustomColors customColors = prefs.getKeyboard().getCustomColors();
        Preference bgPreference = findPreference(customColors.getBackground().getKey());
        Preference fgPreference = findPreference(customColors.getForeground().getKey());
        assert bgPreference != null;
        assert fgPreference != null;
        bgPreference.setVisible(visible);
        fgPreference.setVisible(visible);
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
            openFileSelector();
            return true;
        });
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
        DialogsHelper.createItemsChoice(context, R.string.select_preferred_keyboard_layout_dialog_title,
                availableLayouts.getDisplayNames(),
                availableLayouts.getIndex(),
                (dialog, which, text) -> {
                    if (which != -1) {
                        availableLayouts.selectLayout(context, which);
                    }
                    return null;
                }).show();
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

        PreferenceData<String> emoticonKeyboardPref = prefs.getKeyboard().getEmoticonKeyboard();
        String selectedKeyboardId = emoticonKeyboardPref.get();
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                emoticonKeyboardPref.reset();
            }
        }
        DialogsHelper.createItemsChoice(context, R.string.select_preferred_emoticon_keyboard_dialog_title,
                inputMethodsNameAndId.keySet(),
                selectedKeyboardIndex,
                (dialog, which, text) -> {
                    if (which != -1) {
                        emoticonKeyboardPref.set(keyboardIds.get(which), true);
                    }
                    return null;
                }).show();
    }


}
