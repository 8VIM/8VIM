package inc.flide.vim8.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.graphics.PointF;
import inc.flide.vim8.structures.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GeometricUtilitiesTest {
    private PointF mockPointF(float x, float y) {
        PointF point = mock(PointF.class);
        point.x = x;
        point.y = y;
        return point;
    }

    @Test
    void getSquaredDistanceBetweenPointsTest() {
        PointF a = mockPointF(2f, 2f);
        PointF b = mockPointF(0f, 0f);

        double distance = GeometricUtilities.getSquaredDistanceBetweenPoints(a, b);
        assertThat(distance).isEqualTo(8);
    }

    @Test
    void getBaseQuadrantTest() {
        Direction quadrant = GeometricUtilities.getBaseQuadrant(6);
        assertThat(quadrant).isEqualTo(Direction.LEFT);
    }
}
