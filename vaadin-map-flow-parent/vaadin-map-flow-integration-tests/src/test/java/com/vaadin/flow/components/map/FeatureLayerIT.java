/**
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/feature-layer")
public class FeatureLayerIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement addFeature;
    private TestBenchElement removeFirstFeature;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).first();
        addFeature = $("button").id("add-feature");
        removeFirstFeature = $("button").id("remove-first-feature");
    }

    @Test
    public void addFeatures() {
        final int expectedFeatures = 10;

        for (int i = 0; i < expectedFeatures; i++) {
            addFeature.click();
        }

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(expectedFeatures, features.getLength());
    }

    @Test
    public void removeFeatures() {
        addFeature.click();
        addFeature.click();
        addFeature.click();
        removeFirstFeature.click();
        removeFirstFeature.click();
        removeFirstFeature.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(0, features.getLength());
    }

    private MapElement.FeatureCollectionReference getDefaultFeatureLayerFeatures() {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference featureLayer = mapReference.getLayers()
                .getLayer(1);
        MapElement.VectorSourceReference source = featureLayer.getSource()
                .asVectorSource();
        return source.getFeatures();
    }
}
