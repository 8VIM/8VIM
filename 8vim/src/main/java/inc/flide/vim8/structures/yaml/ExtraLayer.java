package inc.flide.vim8.structures.yaml;

import inc.flide.vim8.structures.FingerPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum ExtraLayer {
    FIRST, SECOND, THIRD, FOURTH, FIFTH;

    public static final HashMap<ExtraLayer, List<FingerPosition>> MOVEMENT_SEQUENCES =
            new HashMap<>();

    static {
        List<FingerPosition> movementSequence =
                new ArrayList<>(Arrays.asList(FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE));

        for (ExtraLayer extraLayer : ExtraLayer.values()) {
            switch (extraLayer) {
                case FIRST:
                case FIFTH:
                    movementSequence.add(FingerPosition.BOTTOM);
                    break;
                case SECOND:
                    movementSequence.add(FingerPosition.LEFT);
                    break;
                case THIRD:
                    movementSequence.add(FingerPosition.TOP);
                    break;
                case FOURTH:
                    movementSequence.add(FingerPosition.RIGHT);
                    break;
                default:
            }
            MOVEMENT_SEQUENCES.put(extraLayer, new ArrayList<>(movementSequence));
        }
    }
}
