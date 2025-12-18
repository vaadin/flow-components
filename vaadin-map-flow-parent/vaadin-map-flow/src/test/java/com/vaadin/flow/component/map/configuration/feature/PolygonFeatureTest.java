/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.Polygon;

public class PolygonFeatureTest {
    private static final Coordinate[] outerCoordinates = { new Coordinate(0, 0),
            new Coordinate(10, 0), new Coordinate(10, 10),
            new Coordinate(0, 10), new Coordinate(0, 0) };

    private static final Coordinate[] innerCoordinates = { new Coordinate(2, 2),
            new Coordinate(8, 2), new Coordinate(8, 8), new Coordinate(2, 8),
            new Coordinate(2, 2) };

    @Test
    public void defaults() {
        PolygonFeature polygonFeature = new PolygonFeature();

        // Test default coordinates
        Assert.assertNotNull(polygonFeature.getCoordinates());
        Assert.assertEquals(1,
                polygonFeature.getGeometry().getCoordinates().length);
        Assert.assertEquals(1,
                polygonFeature.getGeometry().getCoordinates()[0].length);
        Assert.assertEquals(0,
                polygonFeature.getGeometry().getCoordinates()[0][0].getX(), 0);
        Assert.assertEquals(0,
                polygonFeature.getGeometry().getCoordinates()[0][0].getY(), 0);

        // Test default style
        Assert.assertNotNull(polygonFeature.getStyle());
        Assert.assertNotNull(polygonFeature.getStyle().getStroke());
        Assert.assertEquals("hsl(214, 100%, 48%)",
                polygonFeature.getStyle().getStroke().getColor());
        Assert.assertEquals(2, polygonFeature.getStyle().getStroke().getWidth(),
                0);

        Assert.assertNotNull(polygonFeature.getStyle().getFill());
        Assert.assertEquals("hsla(214, 100%, 60%, 0.13)",
                polygonFeature.getStyle().getFill().getColor());
    }

    @Test
    public void individualStyleInstances() {
        PolygonFeature polygon1 = new PolygonFeature();
        PolygonFeature polygon2 = new PolygonFeature();

        Assert.assertNotEquals(polygon1.getStyle(), polygon2.getStyle());
    }

    @Test
    public void initializeWithCoordinates() {
        List<Coordinate> coordinates = List.of(outerCoordinates);
        PolygonFeature polygonFeature = new PolygonFeature(coordinates);

        assertCoordinates(new Coordinate[][] { outerCoordinates },
                polygonFeature);
    }

    @Test
    public void setCoordinatesWithList() {
        PolygonFeature polygonFeature = new PolygonFeature();
        List<Coordinate> coordinates = List.of(outerCoordinates);
        polygonFeature.setCoordinates(coordinates);

        assertCoordinates(new Coordinate[][] { outerCoordinates },
                polygonFeature);
    }

    @Test
    public void setCoordinatesWithArray() {
        PolygonFeature polygonFeature = new PolygonFeature();
        Coordinate[][] coordinatesArray = new Coordinate[][] { outerCoordinates,
                innerCoordinates };
        polygonFeature.setCoordinates(coordinatesArray);

        assertCoordinates(coordinatesArray, polygonFeature);
    }

    @Test
    public void setGeometry() {
        PolygonFeature polygonFeature = new PolygonFeature();
        List<Coordinate> coordinates = List.of(outerCoordinates);
        Polygon polygon = new Polygon(coordinates);
        polygonFeature.setGeometry(polygon);

        Assert.assertSame(polygon, polygonFeature.getGeometry());
    }

    @Test(expected = NullPointerException.class)
    public void setGeometry_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        polygonFeature.setGeometry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setGeometry_withNonPolygon_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        polygonFeature.setGeometry(new Point(new Coordinate(0, 0)));
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesList_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        polygonFeature.setCoordinates((List<Coordinate>) null);
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesArray_withNull_throwsException() {
        PolygonFeature polygonFeature = new PolygonFeature();
        polygonFeature.setCoordinates((Coordinate[][]) null);
    }

    private void assertCoordinates(Coordinate[][] expected,
            PolygonFeature feature) {
        Coordinate[][] actual = feature.getGeometry().getCoordinates();

        Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].length, actual[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                Assert.assertEquals(expected[i][j].getX(), actual[i][j].getX(),
                        0);
                Assert.assertEquals(expected[i][j].getY(), actual[i][j].getY(),
                        0);
            }
        }
    }
}
