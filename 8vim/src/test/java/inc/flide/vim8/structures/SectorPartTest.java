package inc.flide.vim8.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

public class SectorPartTest {
    @Data
    Iterable<Tuple.Tuple2<SectorPart, FingerPosition>> toFingerPositionTable() {
        return Table.of(
            Tuple.of(SectorPart.RIGHT, FingerPosition.RIGHT),
            Tuple.of(SectorPart.LEFT, FingerPosition.LEFT),
            Tuple.of(SectorPart.TOP, FingerPosition.TOP),
            Tuple.of(SectorPart.BOTTOM, FingerPosition.BOTTOM)
        );
    }

    @Data
    Iterable<Tuple.Tuple2<SectorPart, SectorPart>> getOppositeTable() {
        return Table.of(
            Tuple.of(SectorPart.RIGHT, SectorPart.LEFT),
            Tuple.of(SectorPart.LEFT, SectorPart.RIGHT),
            Tuple.of(SectorPart.TOP, SectorPart.BOTTOM),
            Tuple.of(SectorPart.BOTTOM, SectorPart.TOP)
        );
    }

    @Data
    Iterable<Tuple.Tuple3<SectorPart, SectorPart, Integer>> getCharacterIndexInStringTable() {
        return Table.of(
            Tuple.of(SectorPart.RIGHT, SectorPart.BOTTOM, 0),
            Tuple.of(SectorPart.BOTTOM, SectorPart.RIGHT, 1),
            Tuple.of(SectorPart.BOTTOM, SectorPart.LEFT, 8),
            Tuple.of(SectorPart.LEFT, SectorPart.BOTTOM, 9),
            Tuple.of(SectorPart.LEFT, SectorPart.TOP, 16),
            Tuple.of(SectorPart.TOP, SectorPart.LEFT, 17),
            Tuple.of(SectorPart.TOP, SectorPart.RIGHT, 24),
            Tuple.of(SectorPart.RIGHT, SectorPart.TOP, 25)
        );
    }

    @Data
    Iterable<Tuple.Tuple2<CharacterPosition, SectorPart[]>> getOppositeSectorPartTable() {
        return Table.of(
            Tuple.of(CharacterPosition.FIRST, new SectorPart[] {SectorPart.RIGHT, SectorPart.BOTTOM}),
            Tuple.of(CharacterPosition.SECOND, new SectorPart[] {SectorPart.TOP, SectorPart.RIGHT}),
            Tuple.of(CharacterPosition.THIRD, new SectorPart[] {SectorPart.LEFT, SectorPart.TOP}),
            Tuple.of(CharacterPosition.FOURTH, new SectorPart[] {SectorPart.BOTTOM, SectorPart.LEFT})
        );
    }

    @Property
    @FromData("toFingerPositionTable")
    public void toFingerPosition(@ForAll SectorPart sectorPart, @ForAll FingerPosition fingerPosition) {
        assertEquals(fingerPosition, SectorPart.toFingerPosition(sectorPart));
    }

    @Property
    @FromData("getOppositeTable")
    public void getOpposite(@ForAll SectorPart sectorPart, @ForAll SectorPart opposite) {
        assertEquals(opposite, SectorPart.getOpposite(sectorPart));
    }

    @Property
    @FromData("getCharacterIndexInStringTable")
    public void getCharacterIndexInString(@ForAll SectorPart sector, @ForAll SectorPart part, @ForAll Integer characterIndex) {
        assertEquals(characterIndex, SectorPart.getCharacterIndexInString(sector, part, CharacterPosition.FIRST));
    }

    @Property
    @FromData("getOppositeSectorPartTable")
    public void getOppositeSectorPart(@ForAll CharacterPosition characterPosition, @ForAll SectorPart[] oppositeSectorPart) {
        SectorPart sector = SectorPart.RIGHT;
        SectorPart part = SectorPart.TOP;
        assertArrayEquals(oppositeSectorPart, SectorPart.getOppositeSectorPart(sector, part, characterPosition));
    }
}
