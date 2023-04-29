package inc.flide.vim8.structures.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.structures.Quadrant;

public class Layers {
    private List<KeyboardAction> hidden;
    private List<Map<Quadrant, List<KeyboardAction>>> visible;

    public Layers() {
        hidden = new ArrayList<>();
        visible = new ArrayList<>();
    }

    public  List<KeyboardAction> getHidden() {
        return hidden;
    }

    public void setHidden( List<KeyboardAction> hidden) {
        this.hidden = hidden;
    }

    public List<Map<Quadrant, List<KeyboardAction>>> getVisible() {
        return visible;
    }

    public void setVisible(List<Map<Quadrant, List<KeyboardAction>>> visible) {
        this.visible = visible;
    }
}
