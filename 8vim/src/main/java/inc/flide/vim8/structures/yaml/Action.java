package inc.flide.vim8.structures.yaml;

import android.view.KeyEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.KeyboardActionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Action {
    @JsonProperty(value = "type")
    public KeyboardActionType actionType = KeyboardActionType.INPUT_TEXT;
    public String lowerCase = "";
    public String upperCase = "";
    @JsonDeserialize(using = FingerPositionDeserializer.class)
    public List<Integer> movementSequence = new ArrayList<>();
    public Flags flags = Flags.empty();
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
                && flags.getValue() == 0;
    }

    public static class FingerPositionDeserializer extends JsonDeserializer<List<Integer>> {
        @Override
        public List<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, NullPointerException {
            JsonNode node = p.getCodec().readTree(p);
            if (!node.isArray())
                throw MismatchedInputException.from(p, (Class<?>) null,
                        "Impossible to deserlize this array of FingerPositions");
            ArrayNode arrayNode = (ArrayNode) node;
            Iterator<JsonNode> iterator = arrayNode.elements();
            List<Integer> result = new ArrayList<Integer>();
            while (iterator.hasNext()) {
                JsonNode current = iterator.next();
                result.add(getFingerPosition(current, p));
            }
            return result;


        }

        private int getFingerPosition(JsonNode node, JsonParser p) throws JacksonException {
            if (node.isInt() && node.intValue() > 0) {
                return node.intValue();
            } else if (node.isInt()) {
                throw MismatchedInputException.from(p, (Class<?>) null, "FingerPosition value must be positive");
            } else if (node.isTextual()) {
                switch (node.textValue().toUpperCase(Locale.ROOT)) {
                    case "NO_TOUCH":
                        return FingerPosition.NO_TOUCH;
                    case "INSIDE_CIRCLE":
                        return FingerPosition.INSIDE_CIRCLE;
                    case "LONG_PRESS":
                        return FingerPosition.LONG_PRESS;
                    case "LONG_PRESS_END":
                        return FingerPosition.LONG_PRESS_END;
                    default:
                        throw MismatchedInputException.from(p, (Class<?>) null, "unknown FingerPosition");
                }
            } else {
                throw MismatchedInputException.from(p, (Class<?>) null,
                        "Impossible to deserializeFingerPosition");
            }
        }
    }
}
