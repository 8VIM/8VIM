package inc.flide.vim8.structures;


import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

class QuadrantTest {

    @Data
    Iterable<Tuple.Tuple2<Quadrant, Integer>> getCharacterIndexInStringTable() {
        return Table.of(
            Tuple.of(new Quadrant(Direction.RIGHT, Direction.BOTTOM), 0),
            Tuple.of(new Quadrant(Direction.BOTTOM, Direction.RIGHT), 1),
            Tuple.of(new Quadrant(Direction.BOTTOM, Direction.LEFT), 8),
            Tuple.of(new Quadrant(Direction.LEFT, Direction.BOTTOM), 9),
            Tuple.of(new Quadrant(Direction.LEFT, Direction.TOP), 16),
            Tuple.of(new Quadrant(Direction.TOP, Direction.LEFT), 17),
            Tuple.of(new Quadrant(Direction.TOP, Direction.RIGHT), 24),
            Tuple.of(new Quadrant(Direction.RIGHT, Direction.TOP), 25)
        );
    }

    @Property
    @FromData("getCharacterIndexInStringTable")
    public void getCharacterIndexInString(@ForAll Quadrant quadrant, @ForAll Integer characterIndex) {
        assertThat(quadrant.getCharacterIndexInString(CharacterPosition.FIRST)).isEqualTo(characterIndex);
    }
}