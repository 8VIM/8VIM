package inc.flide.vim8.utils;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.SectorPart;
import inc.flide.vim8.structures.yaml.ExtraLayer;

public class MovementSequenceHelperTest {
    @Data
    Iterable<Tuple.Tuple2<CharacterPosition, List<FingerPosition>>> defaultLayerSequences() {
        return Table.of(Tuple.of(CharacterPosition.FIRST,
                new ArrayList<>(Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.INSIDE_CIRCLE))),
            Tuple.of(CharacterPosition.SECOND, new ArrayList<>(
                Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP,
                    FingerPosition.INSIDE_CIRCLE))), Tuple.of(CharacterPosition.THIRD, new ArrayList<>(
                Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT,
                    FingerPosition.INSIDE_CIRCLE))), Tuple.of(CharacterPosition.FOURTH, new ArrayList<>(
                Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.TOP, FingerPosition.RIGHT,
                    FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE))));
    }

    @Data
    Iterable<Tuple.Tuple2<ExtraLayer, List<FingerPosition>>> extraLayerMovementSequence() {
        return Table.of(Tuple.of(ExtraLayer.FIRST, new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM,
                FingerPosition.INSIDE_CIRCLE))), Tuple.of(ExtraLayer.SECOND, new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT,
                FingerPosition.INSIDE_CIRCLE))), Tuple.of(ExtraLayer.THIRD, new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT,
                FingerPosition.TOP, FingerPosition.INSIDE_CIRCLE))), Tuple.of(ExtraLayer.FOURTH, new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT,
                FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.INSIDE_CIRCLE))), Tuple.of(ExtraLayer.FIFTH, new ArrayList<>(
            Arrays.asList(FingerPosition.INSIDE_CIRCLE, FingerPosition.BOTTOM, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.RIGHT,
                FingerPosition.TOP, FingerPosition.LEFT, FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE))));
    }

    @Property
    @FromData("defaultLayerSequences")
    public void computeMovementSequence_forDefaultLayer(@ForAll CharacterPosition characterPosition, @ForAll List<FingerPosition> movementSequence) {
        List<FingerPosition> computedMovementSequence =
            MovementSequenceHelper.computeMovementSequence(Constants.DEFAULT_LAYER, Pair.of(SectorPart.BOTTOM, SectorPart.LEFT),
                characterPosition);
        assertThat(computedMovementSequence).containsExactlyElementsOf(movementSequence);
    }

    @Property
    @FromData("extraLayerMovementSequence")
    public void computeMovementSequence_forExtraLayer(@ForAll ExtraLayer extraLayer, @ForAll List<FingerPosition> movementSequence) {
        List<FingerPosition> computedMovementSequence =
            MovementSequenceHelper.computeMovementSequence(extraLayer.ordinal() + 2, Pair.of(SectorPart.BOTTOM, SectorPart.LEFT),
                CharacterPosition.FIRST);
        assertThat(computedMovementSequence).containsExactlyElementsOf(movementSequence);
    }
}