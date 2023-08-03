package inc.flide.vim8.structures

import inc.flide.vim8.models.Direction
import inc.flide.vim8.models.FingerPosition
import net.jqwik.api.Data
import net.jqwik.api.ForAll
import net.jqwik.api.FromData
import net.jqwik.api.Property
import net.jqwik.api.Table
import net.jqwik.api.Tuple
import net.jqwik.api.Tuple.Tuple2

class DirectionTest {
    @Data
    fun toFingerPositionTable(): Iterable<Tuple2<Direction, FingerPosition>> {
        return Table.of(
            Tuple.of(Direction.RIGHT, FingerPosition.RIGHT),
            Tuple.of(Direction.LEFT, FingerPosition.LEFT),
            Tuple.of(Direction.TOP, FingerPosition.TOP),
            Tuple.of(Direction.BOTTOM, FingerPosition.BOTTOM)
        )
    }

    @get:Data
    val oppositeTable: Iterable<Tuple2<Direction, Direction>>
        get() = Table.of(
            Tuple.of(Direction.RIGHT, Direction.LEFT),
            Tuple.of(Direction.LEFT, Direction.RIGHT),
            Tuple.of(Direction.TOP, Direction.BOTTOM),
            Tuple.of(Direction.BOTTOM, Direction.TOP)
        )

    @Property
    @FromData("toFingerPositionTable")
    fun toFingerPosition(@ForAll direction: Direction?, @ForAll fingerPosition: FingerPosition?) {
//        assertThat(Direction.toFingerPosition(direction)).isEqualTo(fingerPosition);
    }

    @Property
    @FromData("getOppositeTable")
    fun getOpposite(@ForAll direction: Direction?, @ForAll opposite: Direction?) {
//        assertThat(Direction.getOpposite(direction)).isEqualTo(opposite);
    }
}