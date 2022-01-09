package inc.flide.vim8.structures

import java.util.HashMap

class KeyboardData {
    private var actionMap: MutableMap<List<FingerPosition>, KeyboardAction> = HashMap()
    private var lowerCaseCharacters: String = ""
    private var upperCaseCharacters: String = ""
    fun getActionMap(): MutableMap<List<FingerPosition>, KeyboardAction> {
        return actionMap
    }

    fun setActionMap(actionMap: MutableMap<List<FingerPosition>, KeyboardAction>) {
        this.actionMap = actionMap
    }

    fun addAllToActionMap(actionMapAddition: MutableMap<List<FingerPosition>, KeyboardAction>) {
        actionMap.putAll(actionMapAddition)
    }

    fun getLowerCaseCharacters(): String {
        return lowerCaseCharacters
    }

    fun setLowerCaseCharacters(lowerCaseCharacters: String) {
        this.lowerCaseCharacters = lowerCaseCharacters
    }

    fun getUpperCaseCharacters(): String {
        return upperCaseCharacters
    }

    fun setUpperCaseCharacters(upperCaseCharacters: String) {
        this.upperCaseCharacters = upperCaseCharacters
    }
}