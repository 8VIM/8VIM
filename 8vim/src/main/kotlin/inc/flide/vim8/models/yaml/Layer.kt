package inc.flide.vim8.models.yaml

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.models.Direction
import java.util.EnumMap

@optics
data class Layer(
    @JsonProperty(required = true) val sectors: Map<Direction, Part> = EnumMap(
        Direction::class.java
    )
) {
    companion object
}