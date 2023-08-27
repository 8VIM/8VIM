package inc.flide.vim8.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty

@optics
data class Layer(
    @JsonProperty(required = true) val sectors: Map<Int, Part> = mapOf()
) {
    companion object
}
