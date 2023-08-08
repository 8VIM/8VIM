package inc.flide.vim8.structures.yaml;

import inc.flide.vim8.structures.FingerPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum ExtraLayer {
    FIRST, SECOND, THIRD, FOURTH, FIFTH;

    public static final HashMap<ExtraLayer, List<Integer>> MOVEMENT_SEQUENCES =
            new HashMap<>();

    static {
        List<Integer> movementSequence =
                new ArrayList<>(Arrays.asList(FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE));

        for (ExtraLayer extraLayer : ExtraLayer.values()) {
            switch (extraLayer) {
                case FIRST:
                case FIFTH:
                    movementSequence.add(5);
                    break;
                case SECOND:
                    movementSequence.add(2);
                    break;
                case THIRD:
                    movementSequence.add(3);
                    break;
                case FOURTH:
                    movementSequence.add(4);
                    break;
                default:
            }
            MOVEMENT_SEQUENCES.put(extraLayer, new ArrayList<>(movementSequence));
        }
    }
}
