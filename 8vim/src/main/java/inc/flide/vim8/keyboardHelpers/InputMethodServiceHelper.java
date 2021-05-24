package inc.flide.vim8.keyboardHelpers;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.ScriptGroup;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.FingerPosition;

public class InputMethodServiceHelper {

    public static Map<List<FingerPosition>, KeyboardAction> initializeKeyboardActionMap(Resources resources, Context context) {

        Map<List<FingerPosition>, KeyboardAction> mainKeyboardActionsMap = new HashMap<>();
        mainKeyboardActionsMap = addToKeyboardActionsMap(
                mainKeyboardActionsMap,
                resources,
                R.raw.sector_circle_as_buttons);
        mainKeyboardActionsMap = addToKeyboardActionsMap(
                mainKeyboardActionsMap,
                resources,
                R.raw.d_pad_actions);
        mainKeyboardActionsMap = addToKeyboardActionsMap(
                mainKeyboardActionsMap,
                resources,
                R.raw.special_core_gestures);

        int languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context);
        mainKeyboardActionsMap = addToKeyboardActionsMap(
                mainKeyboardActionsMap,
                resources,
                languageLayoutResourceId);

        return mainKeyboardActionsMap;
    }

    private static int loadTheSelectedLanguageLayout(Resources resources, Context context) {
        String currentLanguageLayout = SharedPreferenceHelper.getInstance(context).getString("current_language_layout", resources.getResourceName(R.raw.en_eight_pen_esperanto));

        String packageName = currentLanguageLayout.substring(0, currentLanguageLayout.indexOf(':'));
        String defType = currentLanguageLayout.substring(currentLanguageLayout.indexOf(':')+1, currentLanguageLayout.indexOf('/'));
        currentLanguageLayout = currentLanguageLayout.substring(currentLanguageLayout.indexOf('/')+1);

        return resources.getIdentifier(currentLanguageLayout, defType, packageName);
    }

    private static Map<List<FingerPosition>, KeyboardAction> addToKeyboardActionsMap(
        Map<List<FingerPosition>, KeyboardAction> firstKeyboardActionsMap,
        Resources resources, int resourceId) {

        if( firstKeyboardActionsMap == null) {
            firstKeyboardActionsMap = new HashMap<>();
        }
        try (InputStream inputStream = resources.openRawResource(resourceId)){
            KeyboardActionXmlParser keyboardActionXmlParser = new KeyboardActionXmlParser(inputStream);
            Map<List<FingerPosition>, KeyboardAction> tempKeyboardActionMap = keyboardActionXmlParser
                    .readKeyboardActionMap();
            if (validateNoConflictingActions(firstKeyboardActionsMap, tempKeyboardActionMap)) {
                firstKeyboardActionsMap.putAll(tempKeyboardActionMap);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return firstKeyboardActionsMap;
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
