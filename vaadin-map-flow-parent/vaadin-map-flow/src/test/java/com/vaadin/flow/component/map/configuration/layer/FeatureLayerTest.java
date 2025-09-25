/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;
import com.vaadin.flow.component.map.configuration.source.ClusterSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

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
        Assert.assertFalse(featureLayer.isClusteringEnabled());
        Assert.assertEquals(50, featureLayer.getClusterDistance());
        Assert.assertEquals(50, featureLayer.getClusterMinDistance());
        Assert.assertNotNull(featureLayer.getSource());
        Assert.assertFalse(featureLayer.getSource() instanceof ClusterSource);
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

    @Test
    public void enableClustering() {
        MarkerFeature feature1 = new MarkerFeature();
        MarkerFeature feature2 = new MarkerFeature();
        featureLayer.addFeature(feature1);
        featureLayer.addFeature(feature2);

        featureLayer.setClusteringEnabled(true);

        Assert.assertTrue(featureLayer.isClusteringEnabled());
        Assert.assertTrue(featureLayer.getSource() instanceof ClusterSource);
        Assert.assertEquals(2, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(feature1));
        Assert.assertTrue(featureLayer.getFeatures().contains(feature2));
    }

    @Test
    public void enableClustering_copiesSourceProperties() {
        VectorSource.Options options = new VectorSource.Options();
        options.setProjection("EPSG:4326");
        options.setAttributionsCollapsible(false);
        options.setAttributions(List.of("Test Attribution"));
        VectorSource customSource = new VectorSource(options);
        featureLayer.setSource(customSource);

        featureLayer.setClusteringEnabled(true);

        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assert.assertEquals(List.of("Test Attribution"),
                clusterSource.getAttributions());
        Assert.assertEquals(customSource.isAttributionsCollapsible(),
                clusterSource.isAttributionsCollapsible());
        Assert.assertEquals(customSource.getProjection(),
                clusterSource.getProjection());
    }

    @Test
    public void disableClustering() {
        MarkerFeature feature1 = new MarkerFeature();
        MarkerFeature feature2 = new MarkerFeature();
        featureLayer.setClusteringEnabled(true);
        featureLayer.addFeature(feature1);
        featureLayer.addFeature(feature2);

        featureLayer.setClusteringEnabled(false);

        Assert.assertFalse(featureLayer.isClusteringEnabled());
        Assert.assertFalse(featureLayer.getSource() instanceof ClusterSource);
        Assert.assertEquals(2, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(feature1));
        Assert.assertTrue(featureLayer.getFeatures().contains(feature2));
    }

    @Test
    public void setClusterDistance() {
        featureLayer.setClusterDistance(50);
        Assert.assertEquals(50, featureLayer.getClusterDistance());

        featureLayer.setClusteringEnabled(true);
        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assert.assertEquals(50, clusterSource.getDistance());

        featureLayer.setClusterDistance(100);
        Assert.assertEquals(100, clusterSource.getDistance());
    }

    @Test
    public void setClusterMinDistance() {
        featureLayer.setClusterMinDistance(10);
        Assert.assertEquals(10, featureLayer.getClusterMinDistance());

        featureLayer.setClusteringEnabled(true);
        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assert.assertEquals(10, clusterSource.getMinDistance());

        featureLayer.setClusterMinDistance(20);
        Assert.assertEquals(20, clusterSource.getMinDistance());
    }

    @Test
    public void clusteringEnabled_addPointFeature_succeeds() {
        featureLayer.setClusteringEnabled(true);
        MarkerFeature markerFeature = new MarkerFeature();

        featureLayer.addFeature(markerFeature);

        Assert.assertEquals(1, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(markerFeature));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clusteringEnabled_addNonPointFeature_fails() {
        featureLayer.setClusteringEnabled(true);
        PolygonFeature polygonFeature = new PolygonFeature();

        featureLayer.addFeature(polygonFeature);
    }

    @Test
    public void clusteringDisabled_addNonPointFeature_succeeds() {
        PolygonFeature polygonFeature = new PolygonFeature();

        featureLayer.addFeature(polygonFeature);

        Assert.assertEquals(1, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(polygonFeature));
    }

    @Test
    public void enableClustering_removesNonPointFeatures() {
        MarkerFeature markerFeature = new MarkerFeature();
        PolygonFeature polygonFeature = new PolygonFeature();
        featureLayer.addFeature(markerFeature);
        featureLayer.addFeature(polygonFeature);
        Assert.assertEquals(2, featureLayer.getFeatures().size());

        featureLayer.setClusteringEnabled(true);

        Assert.assertTrue(featureLayer.isClusteringEnabled());
        Assert.assertEquals(1, featureLayer.getFeatures().size());
        Assert.assertTrue(featureLayer.getFeatures().contains(markerFeature));
        Assert.assertFalse(featureLayer.getFeatures().contains(polygonFeature));
    }

    private static class TestFeatureLayer extends FeatureLayer {
        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
