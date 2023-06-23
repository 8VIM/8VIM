package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import inc.flide.vim8.structures.Direction;
import java.util.List;
import java.util.Map;

public class Part {
    @JsonProperty(required = true)
    public Map<Direction, List<Action>> parts;
}