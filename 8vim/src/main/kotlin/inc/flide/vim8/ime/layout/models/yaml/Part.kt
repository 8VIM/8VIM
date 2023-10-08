package inc.flide.vim8.ime.layout.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.ime.layout.models.Direction
import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport

@ExcludeFromJacocoGeneratedReport
@optics
data class Part(@JsonProperty(required = true) val parts: Map<Direction, List<Action>> = mapOf()) {
    companion object
}
