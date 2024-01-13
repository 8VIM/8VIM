package inc.flide.vim8.ime.input

enum class KeyVariation(val value: Int) {
    ALL(0),
    NORMAL(1),
    PASSWORD(2);

    companion object {
        fun fromInt(int: Int) = values().firstOrNull { it.value == int } ?: ALL
    }

    fun toInt() = value
}
