package inc.flide.vim8.keyboardHelpers;

import android.content.Context;
import android.content.res.Resources;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardData;

public class InputMethodServiceHelper {

    public static KeyboardData initializeKeyboardActionMap(Resources resources, Context context) {

        KeyboardData mainKeyboardData = new KeyboardData();
        addToKeyboardActionsMap(
                mainKeyboardData,
                resources,
                R.raw.sector_circle_buttons);
        addToKeyboardActionsMap(
                mainKeyboardData,
                resources,
                R.raw.d_pad_actions);
        addToKeyboardActionsMap(
                mainKeyboardData,
                resources,
                R.raw.special_core_gestures);

        int languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context);
        addToKeyboardActionsMap(
                mainKeyboardData,
                resources,
                languageLayoutResourceId);

        return mainKeyboardData;
    }

    private static int loadTheSelectedLanguageLayout(Resources resources, Context context) {
        String currentLanguageLayout = SharedPreferenceHelper
                .getInstance(context)
                .getString("current_language_layout",
                        resources.getResourceName(R.raw.en_regular_8pen
                        ));

        String packageName = currentLanguageLayout.substring(0, currentLanguageLayout.indexOf(':'));
        String defType = currentLanguageLayout.substring(currentLanguageLayout.indexOf(':')+1, currentLanguageLayout.indexOf('/'));
        currentLanguageLayout = currentLanguageLayout.substring(currentLanguageLayout.indexOf('/')+1);

        return resources.getIdentifier(currentLanguageLayout, defType, packageName);
    }

    private static void addToKeyboardActionsMap(
        KeyboardData keyboardData,
        Resources resources, int resourceId) {

        try (InputStream inputStream = resources.openRawResource(resourceId)){
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
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static boolean validateNoConflictingActions(
            Map<List<FingerPosition>, KeyboardAction> mainKeyboardActionsMap,
            Map<List<FingerPosition>, KeyboardAction> newKeyboardActionsMap) {

        if (mainKeyboardActionsMap == null || mainKeyboardActionsMap.isEmpty()) {
            return true;
        }
        for (Map.Entry<List<FingerPosition>, KeyboardAction> newKeyboardAction: newKeyboardActionsMap.entrySet()) {
            if(mainKeyboardActionsMap.containsKey(newKeyboardAction.getKey())) {
                return false;
            }
        }

        return true;
    }
}
