package inc.flide.vim8.arbitaries;

import inc.flide.vim8.models.FingerPosition;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;

public class MovementSequenceArbitrary implements ArbitrarySupplier<List<FingerPosition>> {
    @Override
    public Arbitrary<List<FingerPosition>> get() {
        return Arbitraries.of(FingerPosition.class).list().ofMinSize(1).ofMaxSize(10);
    }
}
