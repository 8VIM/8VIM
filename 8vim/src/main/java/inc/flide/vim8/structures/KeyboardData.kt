package inc.flide.vim8.structures

import java.util.HashMap

class KeyboardData {
    private var actionMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?
    private var lowerCaseCharacters: String?
    private var upperCaseCharacters: String?
    fun getActionMap(): MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>? {
        return actionMap
    }

    fun setActionMap(actionMap: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?) {
        this.actionMap = actionMap
    }

    fun addAllToActionMap(actionMapAddition: MutableMap<MutableList<FingerPosition?>?, KeyboardAction?>?) {
        if (actionMapAddition != null) {
            actionMap?.putAll(actionMapAddition)
        }
    }

    fun getLowerCaseCharacters(): String? {
        return lowerCaseCharacters
    }

    fun setLowerCaseCharacters(lowerCaseCharacters: String?) {
        this.lowerCaseCharacters = lowerCaseCharacters
    }

    fun getUpperCaseCharacters(): String? {
        return upperCaseCharacters
    }

    fun setUpperCaseCharacters(upperCaseCharacters: String?) {
        this.upperCaseCharacters = upperCaseCharacters
    }

    init {
        actionMap = HashMap()
        lowerCaseCharacters = ""
        upperCaseCharacters = ""
    }
}