package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import inc.flide.vim8.structures.SectorPart;
import java.util.Map;

public class Layer {
    @JsonProperty(required = true)
    private Map<SectorPart, Part> sectors;

    public Map<SectorPart, Part> getSectors() {
        return sectors;
    }

    public void setSectors(
            Map<SectorPart, Part> sectors) {
        this.sectors = sectors;
    }
}
