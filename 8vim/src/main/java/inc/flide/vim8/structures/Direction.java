package inc.flide.vim8.structures;


public enum Direction {
    RIGHT, TOP, LEFT, BOTTOM;

    public static FingerPosition toFingerPosition(Direction direction) {
        if (direction == RIGHT) {
            return FingerPosition.RIGHT;
        } else if (direction == TOP) {
            return FingerPosition.TOP;
        } else if (direction == LEFT) {
            return FingerPosition.LEFT;
        } else {
            return FingerPosition.BOTTOM;
        }
    }

    public static Direction getOpposite(Direction direction) {
        if (direction == RIGHT) {
            return LEFT;
        } else if (direction == TOP) {
            return BOTTOM;
        } else if (direction == LEFT) {
            return RIGHT;
        } else {
            return TOP;
        }
    }

}
