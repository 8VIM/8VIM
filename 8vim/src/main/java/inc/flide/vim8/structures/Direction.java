package inc.flide.vim8.structures;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Direction {

    public static int toFingerPosition(int direction) {
        return direction;
    }

    public static int getOpposite(int direction, KeyboardData keyboardData) {
        return ((direction - 1)+keyboardData.sectors/2) % keyboardData.sectors + 1;
    }

}
