package inc.flide.vim8.structures;

public enum SectorPart {
    RIGHT, TOP, LEFT, BOTTOM;

    public static FingerPosition toFingerPosition(SectorPart sectorPart) {
        if (sectorPart == RIGHT) {
            return FingerPosition.RIGHT;
        } else if (sectorPart == TOP) {
            return FingerPosition.TOP;
        } else if (sectorPart == LEFT) {
            return FingerPosition.LEFT;
        } else {
            return FingerPosition.BOTTOM;
        }
    }

    public static SectorPart getOpposite(SectorPart sectorPart) {
        if (sectorPart == RIGHT) {
            return LEFT;
        } else if (sectorPart == TOP) {
            return BOTTOM;
        } else if (sectorPart == LEFT) {
            return RIGHT;
        } else {
            return TOP;
        }
    }

    public static int getCharacterIndexInString(SectorPart sector, SectorPart part, CharacterPosition characterPosition) {
        int index = 0;
        switch (sector) {
            case RIGHT:
                index = part == BOTTOM ? 0 : 7;
                break;
            case TOP:
                index = part == LEFT ? 5 : 6;
                break;
            case LEFT:
                index = part == BOTTOM ? 3 : 4;
                break;
            case BOTTOM:
                index = part == RIGHT ? 1 : 2;
                break;
        }
        int base = index / 2 * (Constants.NUMBER_OF_SECTORS * 2);
        int delta = index % 2;
        return base + characterPosition.ordinal() * 2 + delta;
    }

    public static SectorPart[] getOppositeSectorPart(SectorPart sector, SectorPart part, CharacterPosition position) {
        if (position == CharacterPosition.FIRST) {
            return new SectorPart[] {sector, SectorPart.getOpposite(part)};
        } else if (position == CharacterPosition.SECOND) {
            return new SectorPart[] {part, sector};
        } else if (position == CharacterPosition.THIRD) {
            return new SectorPart[] {SectorPart.getOpposite(sector), part};
        } else {
            return new SectorPart[] {SectorPart.getOpposite(part), SectorPart.getOpposite(sector)};
        }
    }
}
