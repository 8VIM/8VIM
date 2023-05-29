package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import inc.flide.vim8.structures.Direction;
import java.util.Map;

public class Layer {
    @JsonProperty(required = true)
    private Map<Direction, Part> sectors;

    public Map<Direction, Part> getSectors() {
        return sectors;
    }

    public void setSectors(
            Map<Direction, Part> sectors) {
        this.sectors = sectors;
    }
}
