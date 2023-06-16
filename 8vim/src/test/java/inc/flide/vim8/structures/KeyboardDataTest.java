package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;

import inc.flide.vim8.arbitaries.KeyboardActionArbitrary;
import inc.flide.vim8.arbitaries.KeyboardActionsArbitrary;
import inc.flide.vim8.arbitaries.MovementSequenceArbitrary;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LowerChars;
import net.jqwik.api.constraints.Size;

public class KeyboardDataTest {

    @Property
    void addActionMap(@ForAll(supplier = MovementSequenceArbitrary.class) List<FingerPosition> movementSequence,
                      @ForAll(supplier = KeyboardActionArbitrary.class) KeyboardAction keyboardAction) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.addActionMap(movementSequence, keyboardAction);
        assertThat(keyboardData.getActionMap()).containsEntry(movementSequence, keyboardAction);
    }

    @Property
    void addAllToActionMap(@ForAll(supplier = KeyboardActionsArbitrary.class)
                           Map<List<FingerPosition>, KeyboardAction> keyboardActions) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.addAllToActionMap(keyboardActions);
        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(keyboardActions);
    }

    @Property
    void findLayer(@ForAll(supplier = MovementSequenceArbitrary.class) List<FingerPosition> movementSequence,
                   @ForAll(supplier = KeyboardActionArbitrary.class) KeyboardAction keyboardAction) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.addActionMap(movementSequence, keyboardAction);
        assertThat(keyboardData.findLayer(movementSequence)).isEqualTo(keyboardAction.getLayer());
        assertThat(keyboardData.findLayer(Collections.emptyList())).isEqualTo(Constants.DEFAULT_LAYER);
    }

    @Property
    void getCaseCharacters(@ForAll @LowerChars @Size(32) String characters,
                           @ForAll @IntRange(min = -1, max = Constants.MAX_LAYERS + 1) int layer) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.setLowerCaseCharacters(characters, layer);
        keyboardData.setUpperCaseCharacters(characters, layer);
        String expected = layer == -1 || layer == Constants.MAX_LAYERS + 1 ? "" : characters;
        assertThat(keyboardData.getLowerCaseCharacters(layer)).isEqualTo(expected);
        assertThat(keyboardData.getUpperCaseCharacters(layer)).isEqualTo(expected);
    }

    @Property
    void getTotalLayers(@ForAll @LowerChars @Size(32) String characters,
                        @ForAll @IntRange(min = 1, max = Constants.MAX_LAYERS) int layer) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.setLowerCaseCharacters(characters, layer);
        assertThat(keyboardData.getTotalLayers()).isEqualTo(layer);
    }
}
