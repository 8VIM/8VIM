package inc.flide.vim8.arbitaries;

import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.models.KeyboardAction;
import inc.flide.vim8.models.KeyboardActionType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.Combinators;

public class KeyboardActionArbitrary implements ArbitrarySupplier<KeyboardAction> {
    @Override
    public Arbitrary<KeyboardAction> get() {
        Arbitrary<String> characterArbitrary = Arbitraries.strings().withCharRange('a', 'z').ofLength(1);
        Arbitrary<KeyboardActionType> keyboardActionTypeArbitrary = Arbitraries.of(KeyboardActionType.class);
        Arbitrary<Integer> keyEventCodeArbitrary = Arbitraries.integers().between(-16, 304);
        Arbitrary<Integer> flagsArbitrary = Arbitraries.integers().between(0, 5);
        Arbitrary<Integer> layersArbitrary =
                Arbitraries.integers().between(Constants.DEFAULT_LAYER + 1, Constants.MAX_LAYERS);

        return Combinators
                .combine(keyboardActionTypeArbitrary,
                        characterArbitrary,
                        characterArbitrary,
                        keyEventCodeArbitrary,
                        flagsArbitrary,
                        layersArbitrary)
                .as(KeyboardAction::new);
    }
}
