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
            Tuple.of(new Quadrant(4, 1), 0),
            Tuple.of(new Quadrant(1, 4), 1),
            Tuple.of(new Quadrant(1, 2), 8),
            Tuple.of(new Quadrant(2, 1), 9),
            Tuple.of(new Quadrant(2, 3), 16),
            Tuple.of(new Quadrant(3, 2), 17),
            Tuple.of(new Quadrant(3, 4), 24),
            Tuple.of(new Quadrant(4, 3), 25)
        );
    }

    @Property
    @FromData("getCharacterIndexInStringTable")
    public void getCharacterIndexInString(@ForAll Quadrant quadrant, @ForAll Integer characterIndex) {
        assertThat(quadrant.getCharacterIndexInString(0)).isEqualTo(characterIndex);
    }
}