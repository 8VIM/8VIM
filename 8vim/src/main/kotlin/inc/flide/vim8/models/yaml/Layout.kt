package inc.flide.vim8.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty

@optics
data class Layout(
    @JsonProperty(required = true) val layers: Layers = Layers(),
    val info: LayoutInfo = LayoutInfo()
) {
    companion object
}

@optics
data class LayoutInfo(
    val name: String = "",
    val description: String = "",
    val rtl: Boolean = false,
    val contact: Contact = Contact()
) {
    companion object
}

@optics
data class Contact(val name: String = "", val email: String = "") {
    companion object
}
