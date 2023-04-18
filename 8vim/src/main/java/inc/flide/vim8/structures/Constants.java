package inc.flide.vim8.structures;

public final class Constants {
    public static final String SELF_KEYBOARD_ID = "inc.flide.vi8/inc.flide.vim8.MainInputMethodService";
    public static final int DELAY_MILLIS_LONG_PRESS_INITIATION = 500;
    public static final int DELAY_MILLIS_LONG_PRESS_CONTINUATION = 50;
    public static final int DEFAULT_LAYER = 1;
    public static final int MAX_LAYERS = 6;
    public static final int DEFAULT_POSITION = -1;
    public static final int CHARACTER_SET_SIZE = 4 * 4 * 2; // 4 sectors, 2 parts, 4 characters per parts
    private Constants() {
    }
}
