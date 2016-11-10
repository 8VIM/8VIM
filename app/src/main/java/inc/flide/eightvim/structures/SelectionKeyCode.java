package inc.flide.eightvim.structures;

public enum SelectionKeyCode {
    MOVE_CURRENT_END_POINT_LEFT(0),
    MOVE_CURRENT_END_POINT_RIGHT(1),
    MOVE_CURRENT_END_POINT_UP(2),
    MOVE_CURRENT_END_POINT_DOWN(3),
    SWITCH_TO_MAIN_KEYBOARD(4);

    private final int value;

    SelectionKeyCode(final int value){
        this.value = value;
    }

    public static SelectionKeyCode getAssociatedSelectionKeyCode(final int code) {
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
            default:
                return null;
        }
    }
}
