package inc.flide.vim8.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.graphics.PointF;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import inc.flide.vim8.structures.SectorPart;

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
        SectorPart quadrant = GeometricUtilities.getBaseQuadrant(6);
        assertThat(quadrant).isEqualTo(SectorPart.LEFT);
    }
}
