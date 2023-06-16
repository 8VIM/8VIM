package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import inc.flide.vim8.structures.Direction;
import java.util.Map;

public class Layer {
    @JsonProperty(required = true)
    public Map<Direction, Part> sectors;
}
