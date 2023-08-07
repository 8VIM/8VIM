package inc.flide.vim8.ui.fragments;


import static android.content.Context.INPUT_METHOD_SERVICE;
import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import arrow.core.EitherKt;
import arrow.core.Option;
import inc.flide.vim8.R;
import inc.flide.vim8.datastore.model.PreferenceData;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.CustomLayout;
import inc.flide.vim8.models.LayoutKt;
import inc.flide.vim8.models.error.ExceptionWrapperError;
import inc.flide.vim8.structures.AvailableLayouts;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.theme.ThemeMode;
import inc.flide.vim8.utils.AlertHelper;
import inc.flide.vim8.utils.DialogsHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Pair;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String[] LAYOUT_FILTER = {"application/octet-stream"};
    private AppPrefs prefs;
    private Set<String> customLayoutHistory = new LinkedHashSet<>();
    private Context context;
    private final ActivityResultLauncher<String[]> openContent =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    selectedCustomLayoutFile -> {
                        if (selectedCustomLayoutFile == null || context == null) {
                            return;
                        }
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        context.getContentResolver()
                                .takePersistableUriPermission(selectedCustomLayoutFile, takeFlags);
                        CustomLayout layout = new CustomLayout(selectedCustomLayoutFile);
                        Pair<Integer, String> errorToShow =
                                EitherKt.merge(
                                        LayoutKt
                                                .loadKeyboardData(layout, context)
                                                .mapLeft(error -> {
                                                    int title = R.string.yaml_error_title;
                                                    if (error instanceof ExceptionWrapperError) {
                                                        title = R.string.generic_error_text;
                                                    }
                                                    return Option.fromNullable(new Pair<>(title, error.getMessage()));
                                                })
                                                .map(keyboardData -> {
                                                    if (keyboardData.getTotalLayers() == 0) {
                                                        return Option.fromNullable(
                                                                new Pair<>(R.string.yaml_error_title,
                                                                        "The layout requires at least one layer"));
                                                    }
                                                    return Option.<Pair<Integer, String>>fromNullable(null);
                                                })).getOrNull();
                        if (errorToShow != null) {
                            AlertHelper.showAlert(context, errorToShow.getFirst(), errorToShow.getSecond());
                            return;
                        }
                        List<String> history = new ArrayList<>(customLayoutHistory);
                        history.add(0, selectedCustomLayoutFile.toString());
                        customLayoutHistory = new LinkedHashSet<>(history);
                        prefs.getLayout().getCurrent().set(layout, true);
                        prefs.getLayout().getCustom().getHistory().set(customLayoutHistory, true);
                    });
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
