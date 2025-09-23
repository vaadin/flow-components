/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/cluster")
public class ClusterIT extends AbstractComponentIT {
    private MapElement map;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
    }

    @Test
    public void clusteringEnabled() {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers()
                .getLayer("feature-layer");
        MapElement.ClusterSourceReference source = layer.getSource()
                .asClusterSource();

        // Layer should have two clusters
        Assert.assertEquals(2, source.getClusterCount());

        // First cluster should have 3 features
        MapElement.ClusterFeatureReference cluster = source.getCluster(0);
        Assert.assertEquals(3, cluster.getFeatureCount());
        Assert.assertNotNull(cluster.getFeature("m1"));
        Assert.assertNotNull(cluster.getFeature("m2"));
        Assert.assertNotNull(cluster.getFeature("m3"));

        // Second cluster should have 1 separate feature
        cluster = source.getCluster(1);
        Assert.assertEquals(1, cluster.getFeatureCount());
        Assert.assertNotNull(cluster.getFeature("m4"));
    }

    @Test
    public void clusteringEnabled_zoomedIn() {
        map.getMapReference().getView().setZoom(6);
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers()
                .getLayer("feature-layer");
        MapElement.ClusterSourceReference source = layer.getSource()
                .asClusterSource();

        // Should expand to clusters for individual features
        Assert.assertEquals(4, source.getClusterCount());
    }

    @Test
    public void clusteringDisabled() {
        clickElementWithJs("toggle-clustering");

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers()
                .getLayer("feature-layer");
        MapElement.VectorSourceReference source = layer.getSource()
                .asVectorSource();

        // Layer's source should just have 4 features
        MapElement.FeatureCollectionReference features = source.getFeatures();
        Assert.assertEquals(4, features.getLength());
        Assert.assertNotNull(features.getFeature("m1"));
        Assert.assertNotNull(features.getFeature("m2"));
        Assert.assertNotNull(features.getFeature("m3"));
        Assert.assertNotNull(features.getFeature("m4"));
    }
}
