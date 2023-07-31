package inc.flide.vim8.keyboardhelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import androidx.preference.PreferenceManager;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;
import inc.flide.vim8.structures.LayoutFileName;
import inc.flide.vim8.structures.exceptions.YamlException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class InputMethodServiceHelper {
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

    public static KeyboardData initializeKeyboardActionMap(Resources resources, InputStream inputStream) {

        KeyboardData mainKeyboardData = getLayoutIndependentKeyboardData(resources);
        addToKeyboardActionsMapUsingInputStream(mainKeyboardData, inputStream);
        return mainKeyboardData;
    }

    public static KeyboardData initializeKeyboardActionMap(Resources resources, Context context) {
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context);
        boolean useCustomSelectedKeyboardLayout = sharedPreferenceHelper.getBoolean(
                context.getString(R.string.pref_use_custom_selected_keyboard_layout),
                false);
        if (useCustomSelectedKeyboardLayout) {
            String customKeyboardLayoutString = sharedPreferenceHelper.getString(
                    context.getString(R.string.pref_selected_custom_keyboard_layout_uri),
                    "");
            if (!customKeyboardLayoutString.isEmpty()) {
                Uri customKeyboardLayout = Uri.parse(customKeyboardLayoutString);
                return initializeKeyboardActionMapForCustomLayout(resources, context, customKeyboardLayout);
            }
        }

        KeyboardData mainKeyboardData = getLayoutIndependentKeyboardData(resources);

        int languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context);
        addToKeyboardActionsMapUsingResourceId(mainKeyboardData, resources, languageLayoutResourceId);

        return mainKeyboardData;
    }

    public static KeyboardData initializeKeyboardActionMapForCustomLayout(Resources resources, Context context,
                                                                          Uri customLayoutUri) {
        if (customLayoutUri == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean(
                    context.getString(R.string.pref_use_custom_selected_keyboard_layout),
                    false);
            sharedPreferencesEditor.apply();

            return initializeKeyboardActionMap(resources, context);
        }

        KeyboardData mainKeyboardData = getLayoutIndependentKeyboardData(resources);
        addToKeyboardActionsMapUsingUri(mainKeyboardData, context, customLayoutUri);

        return mainKeyboardData;
    }

    private static KeyboardData getLayoutIndependentKeyboardData(Resources resources) {
        KeyboardData layoutIndependentKeyboardData = new KeyboardData();
        addToKeyboardActionsMapUsingResourceId(layoutIndependentKeyboardData, resources, R.raw.sector_circle_buttons);
        addToKeyboardActionsMapUsingResourceId(layoutIndependentKeyboardData, resources, R.raw.d_pad_actions);
        addToKeyboardActionsMapUsingResourceId(layoutIndependentKeyboardData, resources, R.raw.special_core_gestures);

        return layoutIndependentKeyboardData;
    }

    private static void addToKeyboardActionsMapUsingUri(KeyboardData keyboardData, Context context,
                                                        Uri customLayoutUri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(customLayoutUri)) {
            addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void addToKeyboardActionsMapUsingInputStream(KeyboardData keyboardData, InputStream inputStream)
            throws YamlException {
        KeyboardData tempKeyboardData = KeyboardDataYamlParser.readKeyboardData(inputStream);
        keyboardData.setInfo(tempKeyboardData.getInfo());

        Map<List<FingerPosition>, KeyboardAction> tempKeyboardDataActionMap =
                tempKeyboardData.getActionMap();
        if (validateNoConflictingActions(keyboardData.getActionMap(), tempKeyboardDataActionMap)) {
            keyboardData.addAllToActionMap(tempKeyboardDataActionMap);
        }

        for (int i = 0; i <= Constants.MAX_LAYERS; i++) {
            if (keyboardData.getLowerCaseCharacters(i).isEmpty()
                    && !tempKeyboardData.getLowerCaseCharacters(i).isEmpty()) {
                keyboardData.setLowerCaseCharacters(tempKeyboardData.getLowerCaseCharacters(i), i);
            }
            if (keyboardData.getUpperCaseCharacters(i).isEmpty()
                    && !tempKeyboardData.getUpperCaseCharacters(i).isEmpty()) {
                keyboardData.setUpperCaseCharacters(tempKeyboardData.getUpperCaseCharacters(i), i);
            }
        }
    }

    private static void addToKeyboardActionsMapUsingResourceId(KeyboardData keyboardData, Resources resources,
                                                               int resourceId) {
        try (InputStream inputStream = resources.openRawResource(resourceId)) {
            addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("DiscouragedApi")
    private static int loadTheSelectedLanguageLayout(Resources resources, Context context) {
        String currentLanguageLayout = SharedPreferenceHelper
                .getInstance(context)
                .getString(resources.getString(R.string.pref_selected_keyboard_layout),
                        new LayoutFileName().getResourceName());

        return resources.getIdentifier(currentLanguageLayout, "raw", context.getPackageName());
    }
}
