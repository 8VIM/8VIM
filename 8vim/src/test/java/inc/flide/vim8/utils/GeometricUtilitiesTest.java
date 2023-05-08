package inc.flide.vim8.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import inc.flide.vim8.structures.SectorPart;
import stub.FakePointF;

public class GeometricUtilitiesTest {

    @Test
    public void getSquaredDistanceBetweenPointsTest() {
        FakePointF a = new FakePointF(2f, 2f);
        FakePointF b = new FakePointF(0f, 0f);
        double distance = GeometricUtilities.getSquaredDistanceBetweenPoints(a, b);
        assertThat(distance).isEqualTo(8);
    }

    @Test
    public void getBaseQuadrantTest() {
        SectorPart quadrant = GeometricUtilities.getBaseQuadrant(6);
        assertThat(quadrant).isEqualTo(SectorPart.LEFT);
    }
}
