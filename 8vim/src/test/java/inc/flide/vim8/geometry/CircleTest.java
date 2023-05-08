package inc.flide.vim8.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.PointF;

import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;
import net.jqwik.api.lifecycle.BeforeContainer;

import inc.flide.vim8.structures.FingerPosition;
import stub.FakePointF;

public class CircleTest {
    private static final Circle circle = new Circle();

    @BeforeContainer
    static void setup() {
        circle.setRadius(10f);
    }

    @Data
    Iterable<Tuple.Tuple2<FingerPosition, PointF>> sectorPositions() {
        return Table.of(
            Tuple.of(FingerPosition.TOP, new FakePointF(0f, -10f)),
            Tuple.of(FingerPosition.LEFT, new FakePointF(-10f, 0f)),
            Tuple.of(FingerPosition.BOTTOM, new FakePointF(0f, 10f)),
            Tuple.of(FingerPosition.RIGHT, new FakePointF(10f, 0f))
        );
    }

    @Property
    @FromData("sectorPositions")
    public void getSectorOfPoint(@ForAll FingerPosition expected, @ForAll PointF point) {
        FingerPosition position = circle.getSectorOfPoint(point);
        assertThat(position).isEqualTo(expected);
    }
}
