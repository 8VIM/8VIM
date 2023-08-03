package inc.flide.vim8.geometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.graphics.PointF;
import inc.flide.vim8.models.FingerPosition;
import net.jqwik.api.Data;
import net.jqwik.api.ForAll;
import net.jqwik.api.FromData;
import net.jqwik.api.Property;
import net.jqwik.api.Table;
import net.jqwik.api.Tuple;
import net.jqwik.api.lifecycle.BeforeContainer;

public class CircleTest {
    private static final Circle circle = new Circle();

    @BeforeContainer
    static void setup() {
        circle.setRadius(10f);
    }

    private PointF mockPointF(float x, float y) {
        PointF point = mock(PointF.class);
        point.x = x;
        point.y = y;
        return point;
    }

    @Data
    Iterable<Tuple.Tuple2<FingerPosition, PointF>> sectorPositions() {
        return Table.of(
                Tuple.of(FingerPosition.TOP, mockPointF(0f, -10f)),
                Tuple.of(FingerPosition.LEFT, mockPointF(-10f, 0f)),
                Tuple.of(FingerPosition.BOTTOM, mockPointF(0f, 10f)),
                Tuple.of(FingerPosition.RIGHT, mockPointF(10f, 0f))
        );
    }

    @Property
    @FromData("sectorPositions")
    public void getSectorOfPoint(@ForAll FingerPosition expected, @ForAll PointF point) {
        FingerPosition position = circle.getSectorOfPoint(point);
        assertThat(position).isEqualTo(expected);
    }
}
