package inc.flide.vim8.structures

class KeyboardData {
    var actionMap: MutableMap<List<FingerPosition>, KeyboardAction> = HashMap()
    var lowerCaseCharacters: String = ""
    var upperCaseCharacters: String = ""
}
