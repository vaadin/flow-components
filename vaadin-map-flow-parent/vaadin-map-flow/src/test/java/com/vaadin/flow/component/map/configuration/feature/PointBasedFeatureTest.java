package com.vaadin.flow.component.map.configuration.feature;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;

public class PointBasedFeatureTest {
    private PropertyChangeListener propertyChangeListenerMock;

    @Before
    public void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void defaults() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assert.assertNotNull(feature.getGeometry());
        Assert.assertNotNull(feature.getGeometry().getCoordinates());
        Assert.assertEquals(0, feature.getGeometry().getCoordinates().getX(),
                0);
        Assert.assertEquals(0, feature.getGeometry().getCoordinates().getY(),
                0);
    }

    @Test
    public void setCoordinates() {
        Coordinate customCoordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        TestPointBasedFeature feature = new TestPointBasedFeature();
        feature.addPropertyChangeListener(propertyChangeListenerMock);

        feature.setCoordinates(customCoordinate);
        Assert.assertEquals(customCoordinate.getX(),
                feature.getGeometry().getCoordinates().getX(), 0);
        Assert.assertEquals(customCoordinate.getY(),
                feature.getGeometry().getCoordinates().getY(), 0);
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setCoordinates_failsWithNullValue() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assert.assertThrows(NullPointerException.class,
                () -> feature.setCoordinates(null));
    }

    @Test
    public void setGeometry() {
        Point customPoint = new Point(new Coordinate());
        TestPointBasedFeature feature = new TestPointBasedFeature();
        feature.addPropertyChangeListener(propertyChangeListenerMock);

        feature.setGeometry(customPoint);
        Assert.assertEquals(customPoint, feature.getGeometry());
        // One event each for removing old geometry, and adding new one
        Mockito.verify(propertyChangeListenerMock, Mockito.times(2))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setGeometry_requiresPoint() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> feature.setGeometry(new TestGeometry()));
    }

    @Test
    public void setGeometry_failsWithNullValue() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assert.assertThrows(NullPointerException.class,
                () -> feature.setGeometry(null));
    }

    private static class TestPointBasedFeature extends PointBasedFeature {
        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }

    private static class TestGeometry extends SimpleGeometry {
        @Override
        public String getType() {
            return "test";
        }
    }
}