package inc.flide.vim8.structures.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Layout {
    @JsonProperty(required = true)
    public Layers layers;
    public LayoutInfo info = new LayoutInfo();

    public static class LayoutInfo {
        public String name = "";
        public String description = "";
        public Contact contact = new Contact();

        public static class Contact {
            public String name = "";
            public String email = "";
        }
    }
}
