/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.Coordinate;

public class PolygonTest {

    private PropertyChangeListener propertyChangeListenerMock;

    @Before
    public void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void defaults() {
        final Polygon polygon = new Polygon(List.of(new Coordinate(1, 1),
                new Coordinate(2, 2), new Coordinate(1, 1)));

        Coordinate[][] expected = new Coordinate[][] { { new Coordinate(1, 1),
                new Coordinate(2, 2), new Coordinate(1, 1) } };

        assertCoordinates(expected, polygon);
    }

    @Test
    public void failsWithNullOrEmptyValues() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon((List<Coordinate>) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon(List.of()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon((Coordinate[][]) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new Polygon(new Coordinate[0][]));
    }

    @Test
    public void setCoordinates() {
        final TestPolygon polygon = new TestPolygon(
                List.of(new Coordinate(1, 1), new Coordinate(2, 2),
                        new Coordinate(1, 1)));

        polygon.addPropertyChangeListener(propertyChangeListenerMock);
        polygon.setCoordinates(List.of(new Coordinate(10, 10),
                new Coordinate(20, 20), new Coordinate(10, 10)));

        Coordinate[][] expected = new Coordinate[][] { { new Coordinate(10, 10),
                new Coordinate(20, 20), new Coordinate(10, 10) } };

        assertCoordinates(expected, polygon);

        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setCoordinatesArray() {
        final TestPolygon polygon = new TestPolygon(
                List.of(new Coordinate(1, 1), new Coordinate(2, 2),
                        new Coordinate(1, 1)));

        polygon.addPropertyChangeListener(propertyChangeListenerMock);

        Coordinate[][] newCoordinates = new Coordinate[][] {
                { new Coordinate(10, 10), new Coordinate(20, 20),
                        new Coordinate(20, 10), new Coordinate(10, 10) },
                { new Coordinate(12, 12), new Coordinate(15, 15),
                        new Coordinate(15, 12), new Coordinate(12, 12) } };

        polygon.setCoordinates(newCoordinates);

        assertCoordinates(newCoordinates, polygon);

        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setCoordinates_failsWithNullOrEmptyValues() {
        final Polygon polygon = new Polygon(List.of(new Coordinate()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates((List<Coordinate>) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates(List.of()));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates((Coordinate[][]) null));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> polygon.setCoordinates(new Coordinate[0][]));
    }

    @Test
    public void translate() {
        final Polygon polygon = new Polygon(List.of(new Coordinate(1, 1),
                new Coordinate(2, 2), new Coordinate(1, 1)));

        polygon.translate(10, 10);

        Coordinate[][] expected = new Coordinate[][] { { new Coordinate(11, 11),
                new Coordinate(12, 12), new Coordinate(11, 11) } };

        assertCoordinates(expected, polygon);
    }

    private void assertCoordinates(Coordinate[][] expected, Polygon polygon) {
        Coordinate[][] actual = polygon.getCoordinates();

        Assert.assertNotNull(actual);
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

    private static class TestPolygon extends Polygon {
        public TestPolygon(List<Coordinate> coordinates) {
            super(coordinates);
        }

        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
