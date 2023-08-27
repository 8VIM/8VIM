package inc.flide.vim8.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty

@optics
data class Part(@JsonProperty(required = true) val parts: Map<Int, List<Action>> = mapOf()) {
    companion object
}
