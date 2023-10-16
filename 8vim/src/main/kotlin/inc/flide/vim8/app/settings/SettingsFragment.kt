package inc.flide.vim8.app.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import inc.flide.vim8.R
import inc.flide.vim8.ime.layout.AvailableLayouts
import inc.flide.vim8.layoutLoader
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.utils.DialogsHelper.createItemsChoice
import inc.flide.vim8.utils.InputMethodUtils.listOtherKeyboard

class SettingsFragment : LayoutFileSelector() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        context?.let { context ->
            if (availableLayouts == null) {
                layoutLoader = context.layoutLoader().value
                availableLayouts = AvailableLayouts(layoutLoader!!, context)
            }
        }
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupPreferenceButtonActions()
        setupPreferenceCallbacks()
    }

    private fun setupPreferenceCallbacks() {
        val trailPrefs = prefs.keyboard.trail
        val preferenceTrailColor = findPreference<Preference>(trailPrefs.color.key)
        val colorModePreference = findPreference<Preference>(prefs.theme.mode.key)
        val randomTrailColorPreference = findPreference<Preference>(trailPrefs.useRandomColor.key)!!

        randomTrailColorPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, value: Any ->
                preferenceTrailColor?.isVisible = !(value as Boolean)
                true
            }
        preferenceTrailColor?.isVisible = !trailPrefs.useRandomColor.get()
        setColorsSelectionVisible(prefs.theme.mode.get())
        colorModePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, value: Any? ->
                val mode = ThemeMode.valueOf((value as String?)!!)
                when (mode) {
                    ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )

                    ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )

                    else -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
                setColorsSelectionVisible(mode)
                true
            }
    }

    private fun setColorsSelectionVisible(mode: ThemeMode) {
        val visible = mode === ThemeMode.CUSTOM
        val customColors = prefs.keyboard.customColors
        val bgPreference = findPreference<Preference>(customColors.background.key)
        val fgPreference = findPreference<Preference>(customColors.foreground.key)
        bgPreference?.isVisible = visible
        fgPreference?.isVisible = visible
    }

    private fun setupPreferenceButtonActions() {
        setupEmojiKeyboardPreferenceAction()
        setupLayoutPreferenceAction()
        setupLoadCustomLayoutPreferenceAction()
    }

    private fun setupLoadCustomLayoutPreferenceAction() {
        findPreference<Preference>(getString(R.string.pref_select_custom_keyboard_layout_key))
            ?.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->
            openFileSelector()
            true
        }
    }

    private fun setupLayoutPreferenceAction() {
        findPreference<Preference>(prefs.layout.current.key)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                askUserPreferredKeyboardLayout()
                true
            }
    }

    private fun setupEmojiKeyboardPreferenceAction() {
        findPreference<Preference>(prefs.keyboard.emoticonKeyboard.key)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                askUserPreferredEmoticonKeyboard()
                true
            }
    }

    private fun askUserPreferredKeyboardLayout() {
        availableLayouts?.let {
            createItemsChoice(
                requireContext(),
                R.string.select_preferred_keyboard_layout_dialog_title,
                it.displayNames,
                it.index
            ) { which: Int -> it.selectLayout(which) }.show()
        }
    }

    private fun askUserPreferredEmoticonKeyboard() {
        val inputMethodsNameAndId = listOtherKeyboard(requireContext())
        val keyboardIds = ArrayList(inputMethodsNameAndId.values)
        val emoticonKeyboardPref = prefs.keyboard.emoticonKeyboard
        val selectedKeyboardId = emoticonKeyboardPref.get()
        var selectedKeyboardIndex = -1
        if (selectedKeyboardId.isNotEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId)
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                emoticonKeyboardPref.reset()
            }
        }
        createItemsChoice(
            requireContext(),
            R.string.select_preferred_emoticon_keyboard_dialog_title,
            inputMethodsNameAndId.keys,
            selectedKeyboardIndex
        ) { which: Int -> emoticonKeyboardPref.set(keyboardIds[which]) }.show()
    }
}
