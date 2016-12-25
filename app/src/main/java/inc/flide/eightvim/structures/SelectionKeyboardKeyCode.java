package inc.flide.eightvim.structures;

public enum SelectionKeyboardKeyCode {
    MOVE_CURRENT_END_POINT_LEFT(0),
    MOVE_CURRENT_END_POINT_RIGHT(1),
    MOVE_CURRENT_END_POINT_UP(2),
    MOVE_CURRENT_END_POINT_DOWN(3),
    SWITCH_TO_MAIN_KEYBOARD(4),
    CUT(5),
    COPY(6),
    PASTE(7),
    SELECT_ALL(8),
    TOOGLE_SELECTION_MODE(9),
    DELETE_SELECTION(10),
    BACKSPACE(11),
    ENTER(12),
    SWITCH_TO_EMOJI_KEYBOARD(13);

    private final int value;

    SelectionKeyboardKeyCode(final int value){
        this.value = value;
    }

    public static SelectionKeyboardKeyCode getAssociatedSelectionKeyCode(final int code) {
        switch (code) {
            case 0:
                return MOVE_CURRENT_END_POINT_LEFT;
            case 1:
                return MOVE_CURRENT_END_POINT_RIGHT;
            case 2:
                return MOVE_CURRENT_END_POINT_UP;
            case 3:
                return MOVE_CURRENT_END_POINT_DOWN;
            case 4:
                return SWITCH_TO_MAIN_KEYBOARD;
            case 5:
                return CUT;
            case 6:
                return COPY;
            case 7:
                return PASTE;
            case 8:
                return SELECT_ALL;
            case 9:
                return TOOGLE_SELECTION_MODE;
            case 10:
                return DELETE_SELECTION;
            case 11:
                return BACKSPACE;
            case 12:
                return ENTER;
            case 13:
                return SWITCH_TO_EMOJI_KEYBOARD;
            default:
                return null;
        }
    }
}
