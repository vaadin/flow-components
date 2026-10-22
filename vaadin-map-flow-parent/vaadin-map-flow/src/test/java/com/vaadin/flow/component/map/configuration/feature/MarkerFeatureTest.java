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

import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.style.Icon;

class MarkerFeatureTest {
    private PropertyChangeListener propertyChangeListenerMock;

    @BeforeEach
    void setup() {
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    void defaults() {
        MarkerFeature markerFeature = new MarkerFeature();

        Assertions.assertNotNull(markerFeature.getCoordinates());
        Assertions.assertEquals(0, markerFeature.getCoordinates().getX(), 0);
        Assertions.assertEquals(0, markerFeature.getCoordinates().getY(), 0);

        Assertions.assertNotNull(markerFeature.getIcon());
        Assertions.assertNotNull(markerFeature.getIcon().getImgHandler());
        Assertions.assertEquals(Assets.PIN.getFileName(),
                markerFeature.getIcon().getImgHandler().getUrlPostfix());

        Assertions.assertNotNull(markerFeature.getIcon().getImgSize());
        Assertions.assertEquals(Assets.PIN.getWidth(),
                markerFeature.getIcon().getImgSize().getWidth());
        Assertions.assertEquals(Assets.PIN.getHeight(),
                markerFeature.getIcon().getImgSize().getHeight());

        Assertions.assertNull(markerFeature.getIcon().getSrc());
    }

    @Test
    void initializeWithCustomCoordinates() {
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        MarkerFeature markerFeature = new MarkerFeature(coordinate);

        Assertions.assertNotNull(markerFeature.getCoordinates());
        Assertions.assertEquals(coordinate.getX(),
                markerFeature.getCoordinates().getX(), 0);
        Assertions.assertEquals(coordinate.getY(),
                markerFeature.getCoordinates().getY(), 0);
    }

    @Test
    void initializeWithCustomIcon() {
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        Icon.Options options = new Icon.Options();
        options.setSrc("assets/custom-marker.png");
        Icon icon = new Icon(options);
        MarkerFeature markerFeature = new MarkerFeature(coordinate, icon);

        Assertions.assertNotNull(markerFeature.getIcon());
        Assertions.assertEquals(icon, markerFeature.getIcon());
    }

    @Test
    void constructorDoesNotAcceptNullValues() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarkerFeature(null));
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarkerFeature(new Coordinate(), null));
    }

    @Test
    void setIcon() {
        TestMarkerFeature markerFeature = new TestMarkerFeature();
        markerFeature.addPropertyChangeListener(propertyChangeListenerMock);

        Icon.Options options = new Icon.Options();
        options.setSrc("assets/custom-marker.png");
        Icon icon = new Icon(options);
        markerFeature.setIcon(icon);

        Assertions.assertEquals(icon, markerFeature.getIcon());
        // One event each for removing old icon, and adding new one
        Mockito.verify(propertyChangeListenerMock, Mockito.times(2))
                .propertyChange(Mockito.any());
    }

    @Test
    void setIcon_failsWithNullValue() {
        MarkerFeature markerFeature = new MarkerFeature();

        Assertions.assertThrows(NullPointerException.class,
                () -> markerFeature.setIcon(null));
    }

    private static class TestMarkerFeature extends MarkerFeature {
        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
