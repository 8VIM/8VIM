package inc.flide.vim8.ime.input

enum class ImeUiMode(val value: Int) {
    TEXT(0),
    SYMBOLS(1),
    NUMERIC(2),
    CLIPBOARD(3),
    SELECTION(4);

    companion object {
        fun fromInt(int: Int) = values().firstOrNull { it.value == int } ?: TEXT
    }

    fun toInt() = value
}