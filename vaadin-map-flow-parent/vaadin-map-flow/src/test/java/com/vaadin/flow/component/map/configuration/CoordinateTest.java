package com.vaadin.flow.component.map.configuration;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CoordinateTest {
    @Test
    public void fromLonLat_implicit_to_epgs_3857() {
        TestCase.ALL.forEach(testCase -> {
            Coordinate result = Coordinate.fromLonLat(testCase.epsg_4326.getX(),
                    testCase.epsg_4326.getY());
            Assert.assertEquals(testCase.epsg_3857.getX(), result.getX(),
                    0.00000001);
            Assert.assertEquals(testCase.epsg_3857.getY(), result.getY(),
                    0.00000001);
        });
    }

    @Test
    public void fromLonLat_to_epgs_3857() {
        TestCase.ALL.forEach(testCase -> {
            Coordinate result = Coordinate.fromLonLat(testCase.epsg_4326.getX(),
                    testCase.epsg_4326.getY(), Projection.EPSG_3857);
            Assert.assertEquals(testCase.epsg_3857.getX(), result.getX(),
                    0.00000001);
            Assert.assertEquals(testCase.epsg_3857.getY(), result.getY(),
                    0.00000001);
        });
    }

    private static class TestCase {
        private static final TestCase ZERO = new TestCase(new Coordinate(0, 0),
                new Coordinate(0, 0));
        private static final TestCase CAPE_TOWN = new TestCase(
                new Coordinate(18.424055, -33.924870),
                new Coordinate(2050956.420947266, -4018718.3584788376));
        private static final TestCase LONDON = new TestCase(
                new Coordinate(-81.245277, 42.984923),
                new Coordinate(-9044182.864998462, 5309677.255300862));
        private static final TestCase LOS_ANGELES = new TestCase(
                new Coordinate(-118.243683, 34.052235),
                new Coordinate(-13162826.58108126, 4035818.0688034757));
        private static final TestCase SYDNEY = new TestCase(
                new Coordinate(151.209290, -33.868820),
                new Coordinate(16832541.166012436, -4011201.3286847863));
        private static final TestCase NORILSK = new TestCase(
                new Coordinate(88.189294, 69.355790),
                new Coordinate(9817187.301498296, 10862209.645055125));

        private static final List<TestCase> ALL = List.of(ZERO, CAPE_TOWN,
                LONDON, LOS_ANGELES, SYDNEY, NORILSK);

        private final Coordinate epsg_4326;
        private final Coordinate epsg_3857;

        public TestCase(Coordinate epsg_4326, Coordinate epsg_3857) {
            this.epsg_4326 = epsg_4326;
            this.epsg_3857 = epsg_3857;
        }
    }
}