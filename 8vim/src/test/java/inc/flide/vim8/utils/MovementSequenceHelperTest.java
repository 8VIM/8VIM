package inc.flide.vim8.utils;

import static org.assertj.core.api.Assertions.assertThat;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.yaml.ExtraLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

public class MovementSequenceHelperTest {
    @Data
    Iterable<Tuple.Tuple2<Integer, List<Integer>>> defaultLayerSequences() {
        return Table.of(
                Tuple.of(1,
                        new ArrayList<>(
                                Arrays.asList(FingerPosition.INSIDE_CIRCLE, 1, 2,
                                        FingerPosition.INSIDE_CIRCLE))),
                Tuple.of(2, new ArrayList<>(
                        Arrays.asList(FingerPosition.INSIDE_CIRCLE, 1, 2,
                                3,
                                FingerPosition.INSIDE_CIRCLE))),
                Tuple.of(3, new ArrayList<>(
                        Arrays.asList(FingerPosition.INSIDE_CIRCLE, 1, 2,
                                3, 4,
                                FingerPosition.INSIDE_CIRCLE))),
                Tuple.of(4, new ArrayList<>(
                        Arrays.asList(FingerPosition.INSIDE_CIRCLE, 1, 2,
                                3, 4,
                                1, FingerPosition.INSIDE_CIRCLE))));
    }

    @Data
    Iterable<Tuple.Tuple2<ExtraLayer, List<Integer>>> extraLayerMovementSequence() {
        return Table.of(
                Tuple.of(ExtraLayer.FIRST,
                        new ArrayList<>(
                                Arrays.asList(1, FingerPosition.INSIDE_CIRCLE,
                                        1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2,
                                        FingerPosition.INSIDE_CIRCLE))),
                Tuple.of(ExtraLayer.SECOND, new ArrayList<>(
                        new ArrayList<>(
                                Arrays.asList(1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2,
                                        FingerPosition.INSIDE_CIRCLE,
                                        1, 2, FingerPosition.INSIDE_CIRCLE)))),
                Tuple.of(ExtraLayer.THIRD, new ArrayList<>(
                        new ArrayList<>(
                                Arrays.asList(1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2, 3,
                                        FingerPosition.INSIDE_CIRCLE,
                                        1, 2, FingerPosition.INSIDE_CIRCLE)))),
                Tuple.of(ExtraLayer.FOURTH, new ArrayList<>(
                        new ArrayList<>(
                                Arrays.asList(1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2, 3,
                                        4, FingerPosition.INSIDE_CIRCLE,
                                        1, 2, FingerPosition.INSIDE_CIRCLE)))),
                Tuple.of(ExtraLayer.FIFTH, new ArrayList<>(
                        new ArrayList<>(
                                Arrays.asList(1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2, 3,
                                        4, 1, FingerPosition.INSIDE_CIRCLE,
                                        1, 2, FingerPosition.INSIDE_CIRCLE)))));
    }

    @Property
    @FromData("defaultLayerSequences")
    public void computeMovementSequence_forDefaultLayer(@ForAll Integer characterPosition,
                                                        @ForAll List<Integer> movementSequence) {
        List<FingerPosition> computedMovementSequence =
                MovementSequenceHelper.computeMovementSequence(Constants.DEFAULT_LAYER,
                        new Quadrant(1, 2),
                        characterPosition);
        assertThat(computedMovementSequence).containsExactlyElementsOf(movementSequence);
    }

    @Property
    @FromData("extraLayerMovementSequence")
    public void computeMovementSequence_forExtraLayer(@ForAll ExtraLayer extraLayer,
                                                      @ForAll List<FingerPosition> movementSequence) {
        List<FingerPosition> computedMovementSequence =
                MovementSequenceHelper.computeMovementSequence(extraLayer.ordinal() + 2,
                        new Quadrant(Direction.BOTTOM, Direction.LEFT),
                        1);
        assertThat(computedMovementSequence).containsExactlyElementsOf(movementSequence);
    }
}
