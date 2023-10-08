package inc.flide.vim8.ime.layout.models

import arrow.optics.optics

@optics
data class CharacterSet(
    val lowerCaseCharacters: String = "",
    val upperCaseCharacters: String = ""
) {
    companion object
}

fun CharacterSet.isNotEmpty(): Boolean =
    lowerCaseCharacters.isNotEmpty() || upperCaseCharacters.isNotEmpty()
