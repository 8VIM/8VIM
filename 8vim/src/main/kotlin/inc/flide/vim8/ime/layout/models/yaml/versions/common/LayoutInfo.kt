package inc.flide.vim8.ime.layout.models.yaml.versions.common

import arrow.optics.optics
import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport

@ExcludeFromJacocoGeneratedReport
@optics
data class LayoutInfo(
    val name: String = "",
    val description: String = "",
    val rtl: Boolean = false,
    val contact: Contact = Contact()
) {
    companion object
}

@ExcludeFromJacocoGeneratedReport
@optics
data class Contact(val name: String = "", val email: String = "") {
    companion object
}
