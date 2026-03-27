/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.beans.PropertyChangeListener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.Coordinate;

class PointTest {

    private PropertyChangeListener propertyChangeListenerMock;

    @BeforeEach
    void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    void defaults() {
        Point point = new Point(new Coordinate(1, 1));

        Assertions.assertNotNull(point.getCoordinates());
        Assertions.assertEquals(1, point.getCoordinates().getX(), 0);
        Assertions.assertEquals(1, point.getCoordinates().getY(), 0);
    }

    @Test
    void failsWithNullValue() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new Point(null));
    }

    @Test
    void setCoordinates() {
        TestPoint point = new TestPoint(new Coordinate());

        point.addPropertyChangeListener(propertyChangeListenerMock);
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        point.setCoordinates(coordinate);

        Assertions.assertEquals(coordinate.getX(),
                point.getCoordinates().getX(), 0);
        Assertions.assertEquals(coordinate.getY(),
                point.getCoordinates().getY(), 0);
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    void setCoordinates_failsWithNullValue() {
        Point point = new Point(new Coordinate());

        Assertions.assertThrows(NullPointerException.class,
                () -> point.setCoordinates(null));
    }

    @Test
    void translate() {
        double value = 123.456;
        double delta = value * 2;
        Point point = new Point(new Coordinate(value, value * -1));
        point.translate(-1 * delta, delta);

        Assertions.assertEquals(value * -1, point.getCoordinates().getX(),
                0.00001);
        Assertions.assertEquals(value, point.getCoordinates().getY(), 0.00001);
    }

    private static class TestPoint extends Point {
        public TestPoint(Coordinate coordinates) {
            super(coordinates);
        }

        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
