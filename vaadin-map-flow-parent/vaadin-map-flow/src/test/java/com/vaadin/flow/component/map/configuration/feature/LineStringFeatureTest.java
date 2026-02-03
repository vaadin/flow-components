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
import com.vaadin.flow.component.map.configuration.geometry.LineString;

public class LineStringFeatureTest {
    private static final Coordinate coordinate1 = new Coordinate(0, 0);
    private static final Coordinate coordinate2 = new Coordinate(10, 10);
    private static final Coordinate coordinate3 = new Coordinate(20, 5);

    private static final Coordinate[] testCoordinates = { coordinate1,
            coordinate2, coordinate3 };

    @Test
    public void initializeWithList() {
        LineStringFeature feature = new LineStringFeature(
                List.of(testCoordinates));

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void initializeWithArray() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void initializeWithVarargs() {
        LineStringFeature feature = new LineStringFeature(coordinate1,
                coordinate2, coordinate3);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void defaultStyle() {
        List<Coordinate> coordinates = List.of(testCoordinates);
        LineStringFeature feature = new LineStringFeature(coordinates);

        Assert.assertNotNull(feature.getStyle());
        Assert.assertNotNull(feature.getStyle().getStroke());
        Assert.assertEquals("hsl(214, 100%, 48%)",
                feature.getStyle().getStroke().getColor());
        Assert.assertEquals(2, feature.getStyle().getStroke().getWidth(), 0);
        Assert.assertNull(feature.getStyle().getFill());
        Assert.assertNull(feature.getStyle().getTextStyle());
    }

    @Test
    public void individualStyleInstances() {
        LineStringFeature line1 = new LineStringFeature(testCoordinates);
        LineStringFeature line2 = new LineStringFeature(testCoordinates);

        Assert.assertNotEquals(line1.getStyle(), line2.getStyle());
    }

    @Test
    public void setCoordinatesWithList() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(List.of(testCoordinates));

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void setCoordinatesWithArray() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(testCoordinates);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void setCoordinatesWithVarargs() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        feature.setCoordinates(coordinate1, coordinate2, coordinate3);

        assertCoordinates(testCoordinates, feature);
    }

    @Test
    public void setGeometry() {
        LineStringFeature feature = new LineStringFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        LineString lineString = new LineString(testCoordinates);
        feature.setGeometry(lineString);

        Assert.assertSame(lineString, feature.getGeometry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullList_throwsException() {
        new LineStringFeature((List<Coordinate>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullArray_throwsException() {
        new LineStringFeature((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyList_throwsException() {
        new LineStringFeature(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyArray_throwsException() {
        new LineStringFeature(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateList_throwsException() {
        new LineStringFeature(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateArray_throwsException() {
        new LineStringFeature(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleArgument_throwsException() {
        new LineStringFeature(new Coordinate(0, 0));
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesWithNullList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates((List<Coordinate>) null);
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesWithNullArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateList_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateArray_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinate_throwsException() {
        LineStringFeature feature = new LineStringFeature(testCoordinates);
        feature.setCoordinates(new Coordinate(0, 0));
    }

    private void assertCoordinates(Coordinate[] expected,
            LineStringFeature feature) {
        Coordinate[] actual = feature.getGeometry().getCoordinates();

        Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].getX(), actual[i].getX(), 0);
            Assert.assertEquals(expected[i].getY(), actual[i].getY(), 0);
        }
    }
}
