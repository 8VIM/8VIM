package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Layers {
    public List<Action> hidden = new ArrayList<>();
    @JsonProperty("default")
    public Layer defaultLayer;
    public Map<ExtraLayer, Layer> extraLayers = new HashMap<>();
}
