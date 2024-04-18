package inc.flide.vim8.ime.layout.models.yaml.versions.common

import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.ime.layout.models.Direction
import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport
import java.util.EnumMap

@ExcludeFromJacocoGeneratedReport
@optics
data class Layer(
    @JsonProperty(required = true) val sectors: Map<Direction, Part> = EnumMap(
        Direction::class.java
    )
) {
    companion object
}
