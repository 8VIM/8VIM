package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;


import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;

import org.apache.commons.lang3.tuple.Pair;

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
    Iterable<Tuple.Tuple2<Pair<SectorPart, SectorPart>, Integer>> getCharacterIndexInStringTable() {
        return Table.of(
            Tuple.of(Pair.of(SectorPart.RIGHT, SectorPart.BOTTOM), 0),
            Tuple.of(Pair.of(SectorPart.BOTTOM, SectorPart.RIGHT), 1),
            Tuple.of(Pair.of(SectorPart.BOTTOM, SectorPart.LEFT), 8),
            Tuple.of(Pair.of(SectorPart.LEFT, SectorPart.BOTTOM), 9),
            Tuple.of(Pair.of(SectorPart.LEFT, SectorPart.TOP), 16),
            Tuple.of(Pair.of(SectorPart.TOP, SectorPart.LEFT), 17),
            Tuple.of(Pair.of(SectorPart.TOP, SectorPart.RIGHT), 24),
            Tuple.of(Pair.of(SectorPart.RIGHT, SectorPart.TOP), 25)
        );
    }

    @Data
    Iterable<Tuple.Tuple2<CharacterPosition, Pair<SectorPart, SectorPart>>> getOppositeSectorPartTable() {
        return Table.of(
            Tuple.of(CharacterPosition.FIRST, Pair.of(SectorPart.RIGHT, SectorPart.BOTTOM)),
            Tuple.of(CharacterPosition.SECOND, Pair.of(SectorPart.TOP, SectorPart.RIGHT)),
            Tuple.of(CharacterPosition.THIRD, Pair.of(SectorPart.LEFT, SectorPart.TOP)),
            Tuple.of(CharacterPosition.FOURTH, Pair.of(SectorPart.BOTTOM, SectorPart.LEFT))
        );
    }

    @Property
    @FromData("toFingerPositionTable")
    public void toFingerPosition(@ForAll SectorPart sectorPart, @ForAll FingerPosition fingerPosition) {
        assertThat(SectorPart.toFingerPosition(sectorPart)).isEqualTo(fingerPosition);
    }

    @Property
    @FromData("getOppositeTable")
    public void getOpposite(@ForAll SectorPart sectorPart, @ForAll SectorPart opposite) {
        assertThat(SectorPart.getOpposite(sectorPart)).isEqualTo(opposite);
    }

    @Property
    @FromData("getCharacterIndexInStringTable")
    public void getCharacterIndexInString(@ForAll Pair<SectorPart, SectorPart> sectorParts, @ForAll Integer characterIndex) {
        assertThat(SectorPart.getCharacterIndexInString(sectorParts, CharacterPosition.FIRST)).isEqualTo(characterIndex);
    }

    @Property
    @FromData("getOppositeSectorPartTable")
    public void getOppositeSectorPart(@ForAll CharacterPosition characterPosition, @ForAll Pair<SectorPart, SectorPart> oppositeSectorPart) {
        SectorPart sector = SectorPart.RIGHT;
        SectorPart part = SectorPart.TOP;
        assertThat(SectorPart.getOppositeSectorPart(Pair.of(sector, part), characterPosition)).isEqualTo(oppositeSectorPart);
    }
}
