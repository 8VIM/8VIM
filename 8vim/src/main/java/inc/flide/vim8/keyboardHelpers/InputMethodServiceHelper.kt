package inc.flide.vim8.keyboardHelpers

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.preference.PreferenceManager
import inc.flide.vim8.R
import inc.flide.vim8.R.raw
import inc.flide.vim8.preferences.SharedPreferenceHelper
import inc.flide.vim8.structures.FingerPosition
import inc.flide.vim8.structures.KeyboardAction
import inc.flide.vim8.structures.KeyboardData
import inc.flide.vim8.structures.LayoutFileName
import java.io.InputStream

object InputMethodServiceHelper {
    fun initializeKeyboardActionMap(resources: Resources?, context: Context?): KeyboardData? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val useCustomSelectedKeyboardLayout = sharedPreferences.getBoolean(
                context.getString(R.string.pref_use_custom_selected_keyboard_layout),
                false)
        if (useCustomSelectedKeyboardLayout) {
            val customKeyboardLayoutString = sharedPreferences.getString(
                    context.getString(R.string.pref_selected_custom_keyboard_layout_uri),
                    null)
            if (customKeyboardLayoutString != null && !customKeyboardLayoutString.isEmpty()) {
                val customKeyboardLayout = Uri.parse(customKeyboardLayoutString)
                return initializeKeyboardActionMapForCustomLayout(resources, context, customKeyboardLayout)
            }
        }
        val mainKeyboardData = getLayoutIndependentKeyboardData(resources)
        val languageLayoutResourceId = loadTheSelectedLanguageLayout(resources, context)
        addToKeyboardActionsMapUsingResourceId(
                mainKeyboardData,
                resources,
                languageLayoutResourceId)
        return mainKeyboardData
    }

    fun initializeKeyboardActionMapForCustomLayout(resources: Resources?, context: Context?, customLayoutUri: Uri?): KeyboardData? {
        if (customLayoutUri == null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val sharedPreferencesEditor = sharedPreferences.edit()
            sharedPreferencesEditor.putBoolean(context.getString(R.string.pref_use_custom_selected_keyboard_layout), false)
            sharedPreferencesEditor.apply()
            return initializeKeyboardActionMap(resources, context)
        }
        val mainKeyboardData = getLayoutIndependentKeyboardData(resources)
        addToKeyboardActionsMapUsingUri(
                mainKeyboardData,
                context,
                customLayoutUri)
        return mainKeyboardData
    }

    private fun getLayoutIndependentKeyboardData(resources: Resources?): KeyboardData? {
        val layoutIndependentKeyboardData = KeyboardData()
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.sector_circle_buttons)
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.d_pad_actions)
        addToKeyboardActionsMapUsingResourceId(
                layoutIndependentKeyboardData,
                resources,
                raw.special_core_gestures)
        return layoutIndependentKeyboardData
    }

    private fun loadTheSelectedLanguageLayout(resources: Resources?, context: Context?): Int {
        val currentLanguageLayout: String = SharedPreferenceHelper.Companion.getInstance(context)
                .getString(resources.getString(R.string.pref_selected_keyboard_layout),
                        LayoutFileName().resourceName)
        return resources.getIdentifier(currentLanguageLayout, "raw", context.getPackageName())
    }

    private fun addToKeyboardActionsMapUsingResourceId(keyboardData: KeyboardData?, resources: Resources?, resourceId: Int) {
        try {
            resources.openRawResource(resourceId).use { inputStream -> addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun addToKeyboardActionsMapUsingUri(keyboardData: KeyboardData?, context: Context?, customLayoutUri: Uri?) {
        try {
            context.getContentResolver().openInputStream(customLayoutUri).use { inputStream -> addToKeyboardActionsMapUsingInputStream(keyboardData, inputStream) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun addToKeyboardActionsMapUsingInputStream(keyboardData: KeyboardData?, inputStream: InputStream?) {
        val keyboardDataXmlParser = KeyboardDataXmlParser(inputStream)
        val tempKeyboardData = keyboardDataXmlParser.readKeyboardData()
        if (validateNoConflictingActions(keyboardData.getActionMap(), tempKeyboardData.actionMap)) {
            keyboardData.addAllToActionMap(tempKeyboardData.actionMap)
        }
        if (keyboardData.getLowerCaseCharacters().isEmpty()
                && !tempKeyboardData.lowerCaseCharacters.isEmpty()) {
            keyboardData.setLowerCaseCharacters(tempKeyboardData.lowerCaseCharacters)
        }
        if (keyboardData.getUpperCaseCharacters().isEmpty()
                && !tempKeyboardData.upperCaseCharacters.isEmpty()) {
            keyboardData.setUpperCaseCharacters(tempKeyboardData.upperCaseCharacters)
        }
    }

    private fun validateNoConflictingActions(
            mainKeyboardActionsMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?,
            newKeyboardActionsMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?): Boolean {
        if (mainKeyboardActionsMap == null || mainKeyboardActionsMap.isEmpty()) {
            return true
        }
        for ((key) in newKeyboardActionsMap) {
            if (mainKeyboardActionsMap.containsKey(key)) {
                return false
            }
        }
        return true
    }
}