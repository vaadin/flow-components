package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;

public class FeatureLayerTest {

    private TestFeatureLayer featureLayer;
    private PropertyChangeListener propertyChangeListenerMock;

    @Before
    public void setup() {
        featureLayer = new TestFeatureLayer();
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void defaults() {
        Assert.assertNotNull(featureLayer.getSource());
    }

    @Test
    public void addFeature() {
        MarkerFeature markerFeature = new MarkerFeature();
        featureLayer.addPropertyChangeListener(propertyChangeListenerMock);
        featureLayer.addFeature(markerFeature);

        Assert.assertEquals(1, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(markerFeature));
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void removeFeature() {
        MarkerFeature markerFeature = new MarkerFeature();
        featureLayer.addFeature(markerFeature);

        featureLayer.addPropertyChangeListener(propertyChangeListenerMock);
        featureLayer.removeFeature(markerFeature);

        Assert.assertEquals(0, featureLayer.getFeatures().size());
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    private static class TestFeatureLayer extends FeatureLayer {
        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}