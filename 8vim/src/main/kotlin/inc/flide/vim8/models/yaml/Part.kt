package inc.flide.vim8.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.models.Direction

@optics
data class Part(@JsonProperty(required = true) val parts: Map<Direction, List<Action>> = mapOf()) {
    companion object
}
