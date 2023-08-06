package inc.flide.vim8.structures;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Direction {

    public static int toFingerPosition(int direction) {
        return direction;
    }

    public static int getOpposite(int direction) {
        return ((direction - 1)+Constants.NUMBER_OF_SECTORS/2) % Constants.NUMBER_OF_SECTORS + 1;
    }

}
