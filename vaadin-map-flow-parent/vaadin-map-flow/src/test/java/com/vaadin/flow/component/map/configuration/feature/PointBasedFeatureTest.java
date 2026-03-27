/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import java.beans.PropertyChangeListener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;

class PointBasedFeatureTest {
    private PropertyChangeListener propertyChangeListenerMock;

    @BeforeEach
    void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    void defaults() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assertions.assertNotNull(feature.getGeometry());
        Assertions.assertNotNull(feature.getGeometry().getCoordinates());
        Assertions.assertEquals(0,
                feature.getGeometry().getCoordinates().getX(), 0);
        Assertions.assertEquals(0,
                feature.getGeometry().getCoordinates().getY(), 0);
    }

    @Test
    void setCoordinates() {
        Coordinate customCoordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        TestPointBasedFeature feature = new TestPointBasedFeature();
        feature.addPropertyChangeListener(propertyChangeListenerMock);

        feature.setCoordinates(customCoordinate);
        Assertions.assertEquals(customCoordinate.getX(),
                feature.getGeometry().getCoordinates().getX(), 0);
        Assertions.assertEquals(customCoordinate.getY(),
                feature.getGeometry().getCoordinates().getY(), 0);
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    void setCoordinates_failsWithNullValue() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assertions.assertThrows(NullPointerException.class,
                () -> feature.setCoordinates(null));
    }

    @Test
    void setGeometry() {
        Point customPoint = new Point(new Coordinate());
        TestPointBasedFeature feature = new TestPointBasedFeature();
        feature.addPropertyChangeListener(propertyChangeListenerMock);

        feature.setGeometry(customPoint);
        Assertions.assertEquals(customPoint, feature.getGeometry());
        // One event each for removing old geometry, and adding new one
        Mockito.verify(propertyChangeListenerMock, Mockito.times(2))
                .propertyChange(Mockito.any());
    }

    @Test
    void setGeometry_requiresPoint() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> feature.setGeometry(new TestGeometry()));
    }

    @Test
    void setGeometry_failsWithNullValue() {
        PointBasedFeature feature = new TestPointBasedFeature();

        Assertions.assertThrows(NullPointerException.class,
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

        @Override
        public void translate(double deltaX, double deltaY) {

        }
    }
}
