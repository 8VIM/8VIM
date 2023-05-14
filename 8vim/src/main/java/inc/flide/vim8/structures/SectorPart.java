package inc.flide.vim8.structures;


import org.apache.commons.lang3.tuple.Pair;

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

    public static int getCharacterIndexInString(Pair<SectorPart, SectorPart> sectorParts,
                                                CharacterPosition characterPosition) {
        int index = 0;
        switch (sectorParts.getLeft()) {
            case RIGHT:
                index = sectorParts.getRight() == BOTTOM ? 0 : 7;
                break;
            case TOP:
                index = sectorParts.getRight() == LEFT ? 5 : 6;
                break;
            case LEFT:
                index = sectorParts.getRight() == BOTTOM ? 3 : 4;
                break;
            case BOTTOM:
                index = sectorParts.getRight() == RIGHT ? 1 : 2;
                break;
            default:
                break;
        }
        int base = index / 2 * (Constants.NUMBER_OF_SECTORS * 2);
        int delta = index % 2;
        return base + characterPosition.ordinal() * 2 + delta;
    }

    public static Pair<SectorPart, SectorPart> getOppositeSectorPart(Pair<SectorPart, SectorPart> sectorParts,
                                                                     CharacterPosition position) {
        if (position == CharacterPosition.FIRST) {
            return Pair.of(sectorParts.getLeft(), SectorPart.getOpposite(sectorParts.getRight()));
        } else if (position == CharacterPosition.SECOND) {
            return Pair.of(sectorParts.getRight(), sectorParts.getLeft());
        } else if (position == CharacterPosition.THIRD) {
            return Pair.of(SectorPart.getOpposite(sectorParts.getLeft()), sectorParts.getRight());
        } else {
            return Pair.of(SectorPart.getOpposite(sectorParts.getRight()),
                    SectorPart.getOpposite(sectorParts.getLeft()));
        }
    }
}
