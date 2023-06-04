package inc.flide.vim8.structures;

import java.util.Objects;

public class Quadrant {
    private final Direction sector;
    private final Direction part;

    public Quadrant(Direction sector, Direction part) {
        this.sector = sector;
        this.part = part;
    }

    public int getCharacterIndexInString(CharacterPosition characterPosition) {
        int index = 0;
        switch (sector) {
            case RIGHT:
                index = part == Direction.BOTTOM ? 0 : 7;
                break;
            case TOP:
                index = part == Direction.LEFT ? 5 : 6;
                break;
            case LEFT:
                index = part == Direction.BOTTOM ? 3 : 4;
                break;
            case BOTTOM:
                index = part == Direction.RIGHT ? 1 : 2;
                break;
        }
        int base = index / 2 * (Constants.NUMBER_OF_SECTORS * 2);
        int delta = index % 2;
        return base + characterPosition.ordinal() * 2 + delta;
    }

    public Direction getSector() {
        return sector;
    }

    public Direction getPart() {
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
