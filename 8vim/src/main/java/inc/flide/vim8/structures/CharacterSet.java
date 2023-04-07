package inc.flide.vim8.structures;

import java.util.ArrayList;
import java.util.List;

public class CharacterSet {

    private String lowerCaseCharacters;
    private String upperCaseCharacters;

    public CharacterSet() {
        lowerCaseCharacters = "";
        upperCaseCharacters = "";
    }

    public String getLowerCaseCharacters() {
        return lowerCaseCharacters;
    }

    public void setLowerCaseCharacters(String lowerCaseCharacters) {
        this.lowerCaseCharacters = lowerCaseCharacters;
    }

    public String getUpperCaseCharacters() {
        return upperCaseCharacters;
    }

    public void setUpperCaseCharacters(String upperCaseCharacters) {
        this.upperCaseCharacters = upperCaseCharacters;
    }
}
