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

public class LineFeatureTest {
    private static final Coordinate coordinate1 = new Coordinate(0, 0);
    private static final Coordinate coordinate2 = new Coordinate(10, 10);
    private static final Coordinate coordinate3 = new Coordinate(20, 5);

    private static final Coordinate[] testCoordinates = { coordinate1,
            coordinate2, coordinate3 };

    @Test
    public void initializeWithList() {
        LineFeature lineFeature = new LineFeature(List.of(testCoordinates));

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void initializeWithArray() {
        LineFeature lineFeature = new LineFeature(testCoordinates);

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void initializeWithVarargs() {
        LineFeature lineFeature = new LineFeature(coordinate1, coordinate2,
                coordinate3);

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void defaultStyle() {
        List<Coordinate> coordinates = List.of(testCoordinates);
        LineFeature lineFeature = new LineFeature(coordinates);

        Assert.assertNotNull(lineFeature.getStyle());
        Assert.assertNotNull(lineFeature.getStyle().getStroke());
        Assert.assertEquals("hsl(214, 100%, 48%)",
                lineFeature.getStyle().getStroke().getColor());
        Assert.assertEquals(2, lineFeature.getStyle().getStroke().getWidth(),
                0);
        Assert.assertNull(lineFeature.getStyle().getFill());
        Assert.assertNull(lineFeature.getStyle().getTextStyle());
    }

    @Test
    public void individualStyleInstances() {
        LineFeature line1 = new LineFeature(testCoordinates);
        LineFeature line2 = new LineFeature(testCoordinates);

        Assert.assertNotEquals(line1.getStyle(), line2.getStyle());
    }

    @Test
    public void setCoordinatesWithList() {
        LineFeature lineFeature = new LineFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        lineFeature.setCoordinates(List.of(testCoordinates));

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void setCoordinatesWithArray() {
        LineFeature lineFeature = new LineFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        lineFeature.setCoordinates(testCoordinates);

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void setCoordinatesWithVarargs() {
        LineFeature lineFeature = new LineFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        lineFeature.setCoordinates(coordinate1, coordinate2, coordinate3);

        assertCoordinates(testCoordinates, lineFeature);
    }

    @Test
    public void setGeometry() {
        LineFeature lineFeature = new LineFeature(new Coordinate(0, 0),
                new Coordinate(1, 1));
        LineString lineString = new LineString(testCoordinates);
        lineFeature.setGeometry(lineString);

        Assert.assertSame(lineString, lineFeature.getGeometry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullList_throwsException() {
        new LineFeature((List<Coordinate>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullArray_throwsException() {
        new LineFeature((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyList_throwsException() {
        new LineFeature(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyArray_throwsException() {
        new LineFeature(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateList_throwsException() {
        new LineFeature(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateArray_throwsException() {
        new LineFeature(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleArgument_throwsException() {
        new LineFeature(new Coordinate(0, 0));
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesWithNullList_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates((List<Coordinate>) null);
    }

    @Test(expected = NullPointerException.class)
    public void setCoordinatesWithNullArray_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyList_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyArray_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateList_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateArray_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinate_throwsException() {
        LineFeature lineFeature = new LineFeature(testCoordinates);
        lineFeature.setCoordinates(new Coordinate(0, 0));
    }

    private void assertCoordinates(Coordinate[] expected, LineFeature feature) {
        Coordinate[] actual = feature.getGeometry().getCoordinates();

        Assert.assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].getX(), actual[i].getX(), 0);
            Assert.assertEquals(expected[i].getY(), actual[i].getY(), 0);
        }
    }
}
