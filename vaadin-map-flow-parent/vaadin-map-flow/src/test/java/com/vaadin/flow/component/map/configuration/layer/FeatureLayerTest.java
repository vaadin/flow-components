/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;
import com.vaadin.flow.component.map.configuration.source.ClusterSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

class FeatureLayerTest {

    private TestFeatureLayer featureLayer;
    private PropertyChangeListener propertyChangeListenerMock;

    @BeforeEach
    void setup() {
        featureLayer = new TestFeatureLayer();
        propertyChangeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    void defaults() {
        Assertions.assertFalse(featureLayer.isClusteringEnabled());
        Assertions.assertEquals(50, featureLayer.getClusterDistance());
        Assertions.assertEquals(50, featureLayer.getClusterMinDistance());
        Assertions.assertNotNull(featureLayer.getSource());
        Assertions
                .assertFalse(featureLayer.getSource() instanceof ClusterSource);
    }

    @Test
    void addFeature() {
        MarkerFeature markerFeature = new MarkerFeature();
        featureLayer.addPropertyChangeListener(propertyChangeListenerMock);
        featureLayer.addFeature(markerFeature);

        Assertions.assertEquals(1, featureLayer.getFeatures().size());
        Assertions
                .assertTrue(featureLayer.getFeatures().contains(markerFeature));
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    void removeFeature() {
        MarkerFeature markerFeature = new MarkerFeature();
        featureLayer.addFeature(markerFeature);

        featureLayer.addPropertyChangeListener(propertyChangeListenerMock);
        featureLayer.removeFeature(markerFeature);

        Assertions.assertEquals(0, featureLayer.getFeatures().size());
        Mockito.verify(propertyChangeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    void enableClustering() {
        MarkerFeature feature1 = new MarkerFeature();
        MarkerFeature feature2 = new MarkerFeature();
        featureLayer.addFeature(feature1);
        featureLayer.addFeature(feature2);

        featureLayer.setClusteringEnabled(true);

        Assertions.assertTrue(featureLayer.isClusteringEnabled());
        Assertions
                .assertTrue(featureLayer.getSource() instanceof ClusterSource);
        Assertions.assertEquals(2, featureLayer.getFeatures().size());
        Assertions.assertTrue(featureLayer.getFeatures().contains(feature1));
        Assertions.assertTrue(featureLayer.getFeatures().contains(feature2));
    }

    @Test
    void enableClustering_copiesSourceProperties() {
        VectorSource.Options options = new VectorSource.Options();
        options.setProjection("EPSG:4326");
        options.setAttributionsCollapsible(false);
        options.setAttributions(List.of("Test Attribution"));
        VectorSource customSource = new VectorSource(options);
        featureLayer.setSource(customSource);

        featureLayer.setClusteringEnabled(true);

        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assertions.assertEquals(List.of("Test Attribution"),
                clusterSource.getAttributions());
        Assertions.assertEquals(customSource.isAttributionsCollapsible(),
                clusterSource.isAttributionsCollapsible());
        Assertions.assertEquals(customSource.getProjection(),
                clusterSource.getProjection());
    }

    @Test
    void disableClustering() {
        MarkerFeature feature1 = new MarkerFeature();
        MarkerFeature feature2 = new MarkerFeature();
        featureLayer.setClusteringEnabled(true);
        featureLayer.addFeature(feature1);
        featureLayer.addFeature(feature2);

        featureLayer.setClusteringEnabled(false);

        Assertions.assertFalse(featureLayer.isClusteringEnabled());
        Assertions
                .assertFalse(featureLayer.getSource() instanceof ClusterSource);
        Assertions.assertEquals(2, featureLayer.getFeatures().size());
        Assertions.assertTrue(featureLayer.getFeatures().contains(feature1));
        Assertions.assertTrue(featureLayer.getFeatures().contains(feature2));
    }

    @Test
    void setClusterDistance() {
        featureLayer.setClusterDistance(10);
        Assertions.assertEquals(10, featureLayer.getClusterDistance());

        featureLayer.setClusteringEnabled(true);
        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assertions.assertEquals(10, clusterSource.getDistance());

        featureLayer.setClusterDistance(100);
        Assertions.assertEquals(100, clusterSource.getDistance());
    }

    @Test
    void setClusterMinDistance() {
        featureLayer.setClusterMinDistance(10);
        Assertions.assertEquals(10, featureLayer.getClusterMinDistance());

        featureLayer.setClusteringEnabled(true);
        ClusterSource clusterSource = (ClusterSource) featureLayer.getSource();
        Assertions.assertEquals(10, clusterSource.getMinDistance());

        featureLayer.setClusterMinDistance(20);
        Assertions.assertEquals(20, clusterSource.getMinDistance());
    }

    @Test
    void clusteringEnabled_addPointFeature_succeeds() {
        featureLayer.setClusteringEnabled(true);
        MarkerFeature markerFeature = new MarkerFeature();

        featureLayer.addFeature(markerFeature);

        Assertions.assertEquals(1, featureLayer.getFeatures().size());
        Assertions
                .assertTrue(featureLayer.getFeatures().contains(markerFeature));
    }

    @Test
    void clusteringEnabled_addNonPointFeature_fails() {
        featureLayer.setClusteringEnabled(true);
        PolygonFeature polygonFeature = new PolygonFeature();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> featureLayer.addFeature(polygonFeature));
    }

    @Test
    void clusteringDisabled_addNonPointFeature_succeeds() {
        PolygonFeature polygonFeature = new PolygonFeature();

        featureLayer.addFeature(polygonFeature);

        Assertions.assertEquals(1, featureLayer.getFeatures().size());
        Assertions.assertTrue(
                featureLayer.getFeatures().contains(polygonFeature));
    }

    @Test
    void enableClustering_removesNonPointFeatures() {
        MarkerFeature markerFeature = new MarkerFeature();
        PolygonFeature polygonFeature = new PolygonFeature();
        featureLayer.addFeature(markerFeature);
        featureLayer.addFeature(polygonFeature);
        Assertions.assertEquals(2, featureLayer.getFeatures().size());

        featureLayer.setClusteringEnabled(true);

        Assertions.assertTrue(featureLayer.isClusteringEnabled());
        Assertions.assertEquals(1, featureLayer.getFeatures().size());
        Assertions
                .assertTrue(featureLayer.getFeatures().contains(markerFeature));
        Assertions.assertFalse(
                featureLayer.getFeatures().contains(polygonFeature));
    }

    private static class TestFeatureLayer extends FeatureLayer {
        // Expose method for testing
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }
    }
}
