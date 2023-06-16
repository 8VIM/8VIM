package inc.flide.vim8.structures.yaml;

import android.view.KeyEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardActionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Action {
    @JsonProperty(value = "type")
    public KeyboardActionType actionType = KeyboardActionType.INPUT_TEXT;
    public String lowerCase = "";
    public String upperCase = "";
    public List<FingerPosition> movementSequence = new ArrayList<>();
    public int flags = 0;
    private int keyCode = 0;

    public int getKeyCode() {
        return keyCode;
    }

    @JsonSetter("key_code")
    public void setKeyCode(String keyCodeString) {
        if (keyCodeString == null || keyCodeString.isEmpty()) {
            return;
        }
        keyCodeString = keyCodeString.toUpperCase(Locale.getDefault());
        //Strictly the inputKey has to has to be a Keycode from the KeyEvent class
        //Or it needs to be one of the customKeyCodes
        keyCode = KeyEvent.keyCodeFromString(keyCodeString);
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            try {
                keyCode = CustomKeycode.valueOf(keyCodeString).getKeyCode();
            } catch (IllegalArgumentException error) {
                keyCode = 0;
            }
        }
    }

    public boolean isEmpty() {
        return (lowerCase == null || lowerCase.isEmpty())
                && (upperCase == null || upperCase.isEmpty())
                && movementSequence.isEmpty()
                && flags == 0;
    }
}
