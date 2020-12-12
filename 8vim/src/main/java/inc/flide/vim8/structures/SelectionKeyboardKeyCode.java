package inc.flide.vim8.structures;

public enum SelectionKeyboardKeyCode {
    MOVE_CURRENT_END_POINT_LEFT(0),
    MOVE_CURRENT_END_POINT_RIGHT(1),
    MOVE_CURRENT_END_POINT_UP(2),
    MOVE_CURRENT_END_POINT_DOWN(3),
    SWITCH_TO_SYMBOLS_KEYPAD(4),
    SELECT_ALL(8),
    TOGGLE_SELECTION_MODE(9);

    private final int value;

    SelectionKeyboardKeyCode(final int value) {
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
                return SWITCH_TO_SYMBOLS_KEYPAD;
            case 8:
                return SELECT_ALL;
            case 9:
                return TOGGLE_SELECTION_MODE;
            default:
                return null;
        }
    }
}
