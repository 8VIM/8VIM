package inc.flide.vim8.ime.input

enum class InputShiftState(val value: Int) {
    UNSHIFTED(0),
    SHIFTED(1),
    CAPS_LOCK(2);
    companion object {
        fun fromInt(int: Int) = values().firstOrNull {it.value==int}?:UNSHIFTED
    }
    fun toInt() = value
}