package inc.flide.vim8.keyboardHelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import androidx.preference.PreferenceManager;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.LayoutFileName;

public final class InputMethodServiceHelper {

    public static KeyboardData initializeKeyboardActionMap(Resources resources, Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useCustomSelectedKeyboardLayout = sharedPreferences.getBoolean(
                context.getString(R.string.pref_use_custom_selected_keyboard_layout),
                false);
        if (useCustomSelectedKeyboardLayout) {
            String customKeyboardLayoutString = sharedPreferences.getString(
                    context.getString(R.string.pref_selected_custom_keyboard_layout_uri),
                    null);
            if (customKeyboardLayoutString != null && !customKeyboardLayoutString.isEmpty()) {
                Uri customKeyboardLayout = Uri.parse(customKeyboardLayoutString);
                return initializeKeyboardActionMapForCustomLayout(resources, context, customKeyboardLayout);
            }
        }

        KeyboardData mainKeyboardData = getLayoutIndependentKeyboardData(resources);

        int languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context);
        addToKeyboardActionsMapUsingResourceId(
                mainKeyboardData,
                resources,
                languageLayoutResourceId);

        return mainKeyboardData;
    }

    public static KeyboardData initializeKeyboardActionMapForCustomLayout(Resources resources, Context context, Uri customLayoutUri) {
        if (customLayoutUri == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean(context.getString(R.string.pref_use_custom_selected_keyboard_layout), false);
            sharedPreferencesEditor.apply();

            return initializeKeyboardActionMap(resources, context);
        }

        KeyboardData mainKeyboardData = getLayoutIndependentKeyboardData(resources);
        addToKeyboardActionsMapUsingUri(
                mainKeyboardData,
                context,
                customLayoutUri);

        return mainKeyboardData;
    }
    private static KeyboardData getLayoutIndependentKeyboardData(Resources resources) {
        KeyboardData layoutIndependentKeyboardData = new KeyboardData();
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                R.raw.sector_circle_buttons);
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                R.raw.d_pad_actions);
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                R.raw.special_core_gestures);

        return layoutIndependentKeyboardData;
    }

    private static int loadTheSelectedLanguageLayout(Resources resources, Context context) {
        String currentLanguageLayout = SharedPreferenceHelper
                .getInstance(context)
                .getString(resources.getString(R.string.pref_selected_keyboard_layout),
                        new LayoutFileName().getResourceName());

        return resources.getIdentifier(currentLanguageLayout, "raw", context.getPackageName());
    }

    private static void addToKeyboardActionsMapUsingResourceId(KeyboardData keyboardData, Resources resources, int resourceId) {
        try (InputStream inputStream = resources.openRawResource(resourceId)) {
            addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private static void addToKeyboardActionsMapUsingUri(KeyboardData keyboardData, Context context, Uri customLayoutUri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(customLayoutUri)) {
            addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void addToKeyboardActionsMapUsingInputStream(KeyboardData keyboardData, InputStream inputStream) throws Exception {
        KeyboardDataXmlParser keyboardDataXmlParser = new KeyboardDataXmlParser(inputStream);
        KeyboardData tempKeyboardData = keyboardDataXmlParser.readKeyboardData();
        if (validateNoConflictingActions(keyboardData.getActionMap(), tempKeyboardData.getActionMap())) {
            keyboardData.addAllToActionMap(tempKeyboardData.getActionMap());
        }
        if (keyboardData.getLowerCaseCharacters().isEmpty()
                && !tempKeyboardData.getLowerCaseCharacters().isEmpty()) {
            keyboardData.setLowerCaseCharacters(tempKeyboardData.getLowerCaseCharacters());
        }
        if (keyboardData.getUpperCaseCharacters().isEmpty()
                && !tempKeyboardData.getUpperCaseCharacters().isEmpty()) {
            keyboardData.setUpperCaseCharacters(tempKeyboardData.getUpperCaseCharacters());
        }
    }

    private static boolean validateNoConflictingActions(
            Map<List<FingerPosition>, KeyboardAction> mainKeyboardActionsMap,
            Map<List<FingerPosition>, KeyboardAction> newKeyboardActionsMap) {

        if (mainKeyboardActionsMap == null || mainKeyboardActionsMap.isEmpty()) {
            return true;
        }
        for (Map.Entry<List<FingerPosition>, KeyboardAction> newKeyboardAction : newKeyboardActionsMap.entrySet()) {
            if (mainKeyboardActionsMap.containsKey(newKeyboardAction.getKey())) {
                return false;
            }
        }

        return true;
    }

    private InputMethodServiceHelper() { }
}
