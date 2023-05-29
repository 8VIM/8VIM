package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LowerChars;
import net.jqwik.api.constraints.Size;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeyboardDataTest {

    @Property
    void addActionMap(@ForAll("movementSequence") List<FingerPosition> movementSequence,
                      @ForAll("keyboardAction") KeyboardAction keyboardAction) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.addActionMap(movementSequence, keyboardAction);
        assertThat(keyboardData.getActionMap()).containsEntry(movementSequence, keyboardAction);
    }

    @Property
    void addAllToActionMap(@ForAll("mapKeyboardActions") Map<List<FingerPosition>, KeyboardAction> keyboardActions) {
        KeyboardData keyboardData = new KeyboardData();
        keyboardData.addAllToActionMap(keyboardActions);
        assertThat(keyboardData.getActionMap()).containsAllEntriesOf(keyboardActions);
    }

    @Property
    void findLayer(@ForAll("movementSequence") List<FingerPosition> movementSequence,
                   @ForAll("keyboardAction") KeyboardAction keyboardAction) {
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

    @Provide
    Arbitrary<List<FingerPosition>> movementSequence() {
        return Arbitraries.of(FingerPosition.class).list().ofMinSize(1).ofMaxSize(10);
    }

    @Provide
    Arbitrary<KeyboardAction> keyboardAction() {
        Arbitrary<String> characterArbitrary = Arbitraries.strings().withCharRange('a', 'z').ofLength(1);
        Arbitrary<KeyboardActionType> keyboardActionTypeArbitrary = Arbitraries.of(KeyboardActionType.class);
        Arbitrary<Integer> keyEventCodeArbitrary = Arbitraries.integers().between(-16, 304);
        Arbitrary<Integer> flagsArbitrary = Arbitraries.integers().between(0, 5);
        Arbitrary<Integer> layersArbitrary = Arbitraries.integers().between(Constants.DEFAULT_LAYER + 1, Constants.MAX_LAYERS);

        return Combinators
            .combine(keyboardActionTypeArbitrary,
                characterArbitrary,
                characterArbitrary,
                keyEventCodeArbitrary,
                flagsArbitrary,
                layersArbitrary)
            .as(KeyboardAction::new);
    }

    @Provide
    Arbitrary<Map<List<FingerPosition>, KeyboardAction>> mapKeyboardActions() {
        return Arbitraries.maps(movementSequence(), keyboardAction());
    }
}
