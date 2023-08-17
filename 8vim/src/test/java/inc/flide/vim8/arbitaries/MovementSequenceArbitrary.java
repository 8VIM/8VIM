package inc.flide.vim8.arbitaries;

import inc.flide.vim8.structures.FingerPosition;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

public class MovementSequenceArbitrary implements ArbitrarySupplier<List<Integer>> {
    @Override
    public Arbitrary<List<Integer>> get() {
        return Arbitraries.of(Integer.class).list().ofMinSize(1).ofMaxSize(10);
    }
}
