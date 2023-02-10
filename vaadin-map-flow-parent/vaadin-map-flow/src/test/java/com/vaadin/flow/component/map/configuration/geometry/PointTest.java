package com.vaadin.flow.component.map.configuration.geometry;

import com.vaadin.flow.component.map.configuration.Coordinate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;

public class PointTest {

    private PropertyChangeListener propertyChangeListenerMock;

    @Before
    public void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void defaults() {
        Point point = new Point(new Coordinate(1, 1));

        Assert.assertNotNull(point.getCoordinates());
        Assert.assertEquals(1, point.getCoordinates().getX(), 0);
        Assert.assertEquals(1, point.getCoordinates().getY(), 0);
    }

    @Test
    public void failsWithNullValue() {
        Assert.assertThrows(NullPointerException.class, () -> new Point(null));
    }

    @Test
    public void setCoordinates() {
        TestPoint point = new TestPoint(new Coordinate());

        point.addPropertyChangeListener(propertyChangeListenerMock);
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        point.setCoordinates(coordinate);

        Assert.assertEquals(coordinate.getX(), point.getCoordinates().getX(),
                0);
        Assert.assertEquals(coordinate.getY(), point.getCoordinates().getY(),
                0);
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setCoordinates_failsWithNullValue() {
        Point point = new Point(new Coordinate());

        Assert.assertThrows(NullPointerException.class,
                () -> point.setCoordinates(null));
    }

    @Test
    public void translate() {
        double value = 123.456;
        double delta = value * 2;
        Point point = new Point(new Coordinate(value, value * -1));
        point.translate(-1 * delta, delta);

        Assert.assertEquals(point.getCoordinates().getX(), value * -1, 0.00001);
        Assert.assertEquals(point.getCoordinates().getY(), value, 0.00001);
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