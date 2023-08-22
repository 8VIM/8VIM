package inc.flide.vim8.models

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
