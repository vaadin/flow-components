/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.geometry.LineString;

class LineStringFeatureTest {
    private static final Coordinate coordinate1 = new Coordinate(0, 0);
    private static final Coordinate coordinate2 = new Coordinate(10, 10);
    private static final Coordinate coordinate3 = new Coordinate(20, 5);

    private static final Coordinate[] testCoordinates = { coordinate1,
            coordinate2, coordinate3 };

    @Test
    void initializeWithList() {
        LineStringFeature feature = new LineStringFeature(
                List.of(testCoordinates));

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void initializeWithArray() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void initializeWithVarargs() {
        LineStringFeature feature = new LineStringFeature(coordinate1,
                coordinate2, coordinate3);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void defaultStyle() {
        List<Coordinate> coordinates = List.of(testCoordinates);
        LineStringFeature feature = new LineStringFeature(coordinates);

        Assertions.assertNotNull(feature.getStyle());
        Assertions.assertNotNull(feature.getStyle().getStroke());
        Assertions.assertEquals("hsl(214, 100%, 48%)",
                feature.getStyle().getStroke().getColor());
        Assertions.assertEquals(2, feature.getStyle().getStroke().getWidth(),
                0);
        Assertions.assertNull(feature.getStyle().getFill());
        Assertions.assertNull(feature.getStyle().getTextStyle());
    }

    @Test
    void individualStyleInstances() {
        LineStringFeature line1 = new LineStringFeature(testCoordinates);
        LineStringFeature line2 = new LineStringFeature(testCoordinates);

        Assertions.assertNotEquals(line1.getStyle(), line2.getStyle());
    }

    @Test
    void setCoordinatesWithList() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(List.of(testCoordinates));

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void setCoordinatesWithArray() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(testCoordinates);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void setCoordinatesWithVarargs() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(coordinate1, coordinate2, coordinate3);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    void setGeometry() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        LineString lineString = new LineString(testCoordinates);
        feature.setGeometry(lineString);

        Assertions.assertSame(lineString, feature.getGeometry());
    }

    @Test
    void constructWithNullList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature((List<Coordinate>) null));
    }

    @Test
    void constructWithNullArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature((Coordinate[]) null));
    }

    @Test
    void constructWithEmptyList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature(List.of()));
    }

    @Test
    void constructWithEmptyArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature(new Coordinate[] {}));
    }

    @Test
    void constructWithSingleCoordinateList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature(List.of(new Coordinate(0, 0))));
    }

    @Test
    void constructWithSingleCoordinateArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature(
                        new Coordinate[] { new Coordinate(0, 0) }));
    }

    @Test
    void constructWithSingleArgument_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineStringFeature(new Coordinate(0, 0)));
    }

    @Test
    void setCoordinatesWithNullList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(NullPointerException.class,
                () -> feature.setCoordinates((List<Coordinate>) null));
    }

    @Test
    void setCoordinatesWithNullArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(NullPointerException.class,
                () -> feature.setCoordinates((Coordinate[]) null));
    }

    @Test
    void setCoordinatesWithEmptyList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> feature.setCoordinates(List.of()));
    }

    @Test
    void setCoordinatesWithEmptyArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> feature.setCoordinates(new Coordinate[] {}));
    }

    @Test
    void setCoordinatesWithSingleCoordinateList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> feature.setCoordinates(List.of(new Coordinate(0, 0))));
    }

    @Test
    void setCoordinatesWithSingleCoordinateArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(IllegalArgumentException.class, () -> feature
                .setCoordinates(new Coordinate[] { new Coordinate(0, 0) }));
    }

    @Test
    void setCoordinatesWithSingleCoordinate_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> feature.setCoordinates(new Coordinate(0, 0)));
    }

    private void assertCoordinates(Coordinate[] expected,
            LineStringFeature feature) {
        Coordinate[] actual = feature.getGeometry().getCoordinates();

        Assertions.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i].getX(), actual[i].getX(), 0);
            Assertions.assertEquals(expected[i].getY(), actual[i].getY(), 0);
        }
    }
}
