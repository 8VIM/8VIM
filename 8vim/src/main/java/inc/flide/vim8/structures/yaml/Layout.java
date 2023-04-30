package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonRootName(value = "layers")
public class Layout {
    private List<Action> hidden;
    @JsonProperty("default")
    private Layer defaultLayer;
    private Map<ExtraLayer, Layer> extraLayers;

    public Layout() {
        hidden = new ArrayList<>();
        extraLayers = new HashMap<>();
    }

    public List<Action> getHidden() {
        return hidden;
    }

    public void setHidden(List<Action> hidden) {
        this.hidden = hidden;
    }

    public Layer getDefaultLayer() {
        return defaultLayer;
    }

    public void setDefaultLayer(
        Layer defaultLayer) {
        this.defaultLayer = defaultLayer;
    }

    public Map<ExtraLayer, Layer> getExtraLayers() {
        return extraLayers;
    }

    public void setExtraLayers(
        Map<ExtraLayer, Layer> extraLayers) {
        this.extraLayers = extraLayers;
    }
}
