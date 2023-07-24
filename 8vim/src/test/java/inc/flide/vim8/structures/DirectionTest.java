package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

public class DirectionTest {
    @Data
    Iterable<Tuple.Tuple2<Direction, FingerPosition>> toFingerPositionTable() {
        return Table.of(
                Tuple.of(Direction.RIGHT, FingerPosition.RIGHT),
                Tuple.of(Direction.LEFT, FingerPosition.LEFT),
                Tuple.of(Direction.TOP, FingerPosition.TOP),
                Tuple.of(Direction.BOTTOM, FingerPosition.BOTTOM)
        );
    }

    @Data
    Iterable<Tuple.Tuple2<Direction, Direction>> getOppositeTable() {
        return Table.of(
                Tuple.of(Direction.RIGHT, Direction.LEFT),
                Tuple.of(Direction.LEFT, Direction.RIGHT),
                Tuple.of(Direction.TOP, Direction.BOTTOM),
                Tuple.of(Direction.BOTTOM, Direction.TOP)
        );
    }


    @Property
    @FromData("toFingerPositionTable")
    public void toFingerPosition(@ForAll Direction direction, @ForAll FingerPosition fingerPosition) {
        assertThat(Direction.toFingerPosition(direction)).isEqualTo(fingerPosition);
    }

    @Property
    @FromData("getOppositeTable")
    public void getOpposite(@ForAll Direction direction, @ForAll Direction opposite) {
        assertThat(Direction.getOpposite(direction)).isEqualTo(opposite);
    }

}
