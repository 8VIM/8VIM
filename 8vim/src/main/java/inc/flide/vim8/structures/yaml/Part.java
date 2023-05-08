package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.SectorPart;

public class Part {
    @JsonProperty(required = true)
    private Map<SectorPart, List<Action>> parts;

    public Map<SectorPart, List<Action>> getParts() {
        return parts;
    }

    public void setParts(Map<SectorPart, List<Action>> parts) {
        this.parts = parts;
    }
}
