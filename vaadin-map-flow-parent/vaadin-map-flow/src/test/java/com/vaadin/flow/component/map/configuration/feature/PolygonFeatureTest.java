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
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.Polygon;

class PolygonFeatureTest {
    private static final Coordinate[] outerCoordinates = { new Coordinate(0, 0),
            new Coordinate(10, 0), new Coordinate(10, 10),
            new Coordinate(0, 10), new Coordinate(0, 0) };

    private static final Coordinate[] innerCoordinates = { new Coordinate(2, 2),
            new Coordinate(8, 2), new Coordinate(8, 8), new Coordinate(2, 8),
            new Coordinate(2, 2) };

    @Test
    void defaults() {
        PolygonFeature polygonFeature = new PolygonFeature();

        // Test default coordinates
        Assertions.assertNotNull(polygonFeature.getCoordinates());
        Assertions.assertEquals(1,
                polygonFeature.getGeometry().getCoordinates().length);
        Assertions.assertEquals(1,
                polygonFeature.getGeometry().getCoordinates()[0].length);
        Assertions.assertEquals(0,
                polygonFeature.getGeometry().getCoordinates()[0][0].getX(), 0);
        Assertions.assertEquals(0,
                polygonFeature.getGeometry().getCoordinates()[0][0].getY(), 0);

        // Test default style
        Assertions.assertNotNull(polygonFeature.getStyle());
        Assertions.assertNotNull(polygonFeature.getStyle().getStroke());
        Assertions.assertEquals("hsl(214, 100%, 48%)",
                polygonFeature.getStyle().getStroke().getColor());
        Assertions.assertEquals(2,
                polygonFeature.getStyle().getStroke().getWidth(), 0);

        Assertions.assertNotNull(polygonFeature.getStyle().getFill());
        Assertions.assertEquals("hsla(214, 100%, 60%, 0.13)",
                polygonFeature.getStyle().getFill().getColor());
    }

    @Test
    void individualStyleInstances() {
        PolygonFeature polygon1 = new PolygonFeature();
        PolygonFeature polygon2 = new PolygonFeature();

        Assertions.assertNotEquals(polygon1.getStyle(), polygon2.getStyle());
    }

    @Test
    void initializeWithCoordinates() {
        List<Coordinate> coordinates = List.of(outerCoordinates);
        PolygonFeature polygonFeature = new PolygonFeature(coordinates);

        assertCoordinates(new Coordinate[][] { outerCoordinates },
                polygonFeature);
    }

    @Test
    void setCoordinatesWithList() {
        PolygonFeature polygonFeature = new PolygonFeature();
        List<Coordinate> coordinates = List.of(outerCoordinates);
        polygonFeature.setCoordinates(coordinates);

        assertCoordinates(new Coordinate[][] { outerCoordinates },
                polygonFeature);
    }

    @Test
    void setCoordinatesWithArray() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Coordinate[][] coordinatesArray = new Coordinate[][] { outerCoordinates,
                innerCoordinates };
        polygonFeature.setCoordinates(coordinatesArray);

        assertCoordinates(coordinatesArray, polygonFeature);
    }

    @Test
    void setGeometry() {
        PolygonFeature polygonFeature = new PolygonFeature();
        List<Coordinate> coordinates = List.of(outerCoordinates);
        Polygon polygon = new Polygon(coordinates);
        polygonFeature.setGeometry(polygon);

        Assertions.assertSame(polygon, polygonFeature.getGeometry());
    }

    @Test
    void setGeometry_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Assertions.assertThrows(NullPointerException.class,
                () -> polygonFeature.setGeometry(null));
    }

    @Test
    void setGeometry_withNonPolygon_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> polygonFeature
                        .setGeometry(new Point(new Coordinate(0, 0))));
    }

    @Test
    void setCoordinatesList_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Assertions.assertThrows(NullPointerException.class,
                () -> polygonFeature.setCoordinates((List<Coordinate>) null));
    }

    @Test
    void setCoordinatesArray_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Assertions.assertThrows(NullPointerException.class,
                () -> polygonFeature.setCoordinates((Coordinate[][]) null));
    }

    private void assertCoordinates(Coordinate[][] expected,
            PolygonFeature feature) {
        Coordinate[][] actual = feature.getGeometry().getCoordinates();

        Assertions.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i].length, actual[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                Assertions.assertEquals(expected[i][j].getX(),
                        actual[i][j].getX(), 0);
                Assertions.assertEquals(expected[i][j].getY(),
                        actual[i][j].getY(), 0);
            }
        }
    }
}
