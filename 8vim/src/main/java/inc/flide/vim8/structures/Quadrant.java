package inc.flide.vim8.structures;

import java.util.Objects;

public class Quadrant {
    private final int sector;
    private final int part;

    public Quadrant(int sector, int part) {
        this.sector = sector;
        this.part = part;
    }

    public int getCharacterIndexInString(int characterPosition, int layoutPositions) {
        if ((part-sector+Constants.NUMBER_OF_SECTORS) % Constants.NUMBER_OF_SECTORS == 1)
            return layoutPositions *2*(part-1) + characterPosition * 2;
        else
            return layoutPositions *2*(sector-1) + characterPosition * 2+1;
    }

    public Quadrant getOppositeQuadrant(int position) {
        if (position == 0) {
            return new Quadrant(sector, Direction.getOpposite(part));
        } else if (position == 1) {
            return new Quadrant(part, sector);
        } else if (position == 2) {
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
