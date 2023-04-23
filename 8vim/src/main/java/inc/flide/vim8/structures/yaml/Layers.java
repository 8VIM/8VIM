package inc.flide.vim8.structures.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.Quadrant;

public class Layers {
    private Map<Quadrant, List<KeyboardAction>> hidden;
    private List<Map<Quadrant, List<KeyboardAction>>> visible;

    public Layers() {
        hidden = new HashMap<>();
        visible = new ArrayList<>();
    }

    public Map<Quadrant, List<KeyboardAction>> getHidden() {
        return hidden;
    }

    public void setHidden(Map<Quadrant, List<KeyboardAction>> hidden) {
        this.hidden = hidden;
    }

    public List<Map<Quadrant, List<KeyboardAction>>> getVisible() {
        return visible;
    }

    public void setVisible(List<Map<Quadrant, List<KeyboardAction>>> visible) {
        this.visible = visible;
    }
}
