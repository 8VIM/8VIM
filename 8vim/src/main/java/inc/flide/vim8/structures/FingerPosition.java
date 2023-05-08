package inc.flide.vim8.structures;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FingerPosition {
    NO_TOUCH, INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT, LONG_PRESS, LONG_PRESS_END;

    public static final Set<FingerPosition> VALID_FINGER_POSITIONS = new HashSet<>(Arrays.asList(INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT));
}
