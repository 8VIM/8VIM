package inc.flide.vim8.structures;

import java.util.Objects;

public class Quadrant {
    private final int sector;
    private final int part;

    public Quadrant(int sector, int part) {
        this.sector = sector;
        this.part = part;
    }

    public int getCharacterIndexInString(CharacterPosition characterPosition) {
        /*
        int index=0;
        if ((part-sector+Constants.NUMBER_OF_SECTORS) % Constants.NUMBER_OF_SECTORS == 1)
            index = ((sector) % Constants.NUMBER_OF_SECTORS)*2;
        else
            index = ((sector-1)*2)+1;
        int base = index / 2 * (CharacterPosition.values().length * 2);
        int delta = index % 2;
        return base + characterPosition.ordinal() * 2 + delta;
         */
        if ((part-sector+Constants.NUMBER_OF_SECTORS) % Constants.NUMBER_OF_SECTORS == 1)
            return CharacterPosition.values().length *2*(part-1) + characterPosition.ordinal() * 2;
        else
            return CharacterPosition.values().length *2*(sector-1) + characterPosition.ordinal() * 2+1;
    }

    public Quadrant getOppositeQuadrant(CharacterPosition position) {
        if (position == CharacterPosition.FIRST) {
            return new Quadrant(sector, Direction.getOpposite(part));
        } else if (position == CharacterPosition.SECOND) {
            return new Quadrant(part, sector);
        } else if (position == CharacterPosition.THIRD) {
            return new Quadrant(Direction.getOpposite(sector), part);
        } else {
            return new Quadrant(Direction.getOpposite(part), Direction.getOpposite(sector));
        }
    }

    public int getSector() {
        return sector;
    }

    public int getPart() {
        return part;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quadrant quadrant = (Quadrant) o;
        return sector == quadrant.sector && part == quadrant.part;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sector, part);
    }
}
