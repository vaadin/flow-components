/**
 * Copyright 2000-2026 Vaadin Ltd.
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

@TestPath("vaadin-map/line-string-feature")
public class LineStringFeatureIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement updateCoordinates;
    private TestBenchElement updateStyle;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        updateCoordinates = $("button").id("update-coordinates");
        updateStyle = $("button").id("update-style");
    }

    @Test
    public void initialCoordinates() {
        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate[] coordinates = feature.getGeometry()
                .getCoordinatesArray();
        Assert.assertEquals(4, coordinates.length);

        Assert.assertEquals(-10.0, coordinates[0].getX(), 0.0001);
        Assert.assertEquals(10.0, coordinates[0].getY(), 0.0001);
        Assert.assertEquals(10.0, coordinates[1].getX(), 0.0001);
        Assert.assertEquals(10.0, coordinates[1].getY(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[2].getX(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[2].getY(), 0.0001);
        Assert.assertEquals(10.0, coordinates[3].getX(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[3].getY(), 0.0001);
    }

    @Test
    public void updateCoordinates() {
        updateCoordinates.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate[] coordinates = feature.getGeometry()
                .getCoordinatesArray();
        Assert.assertEquals(5, coordinates.length);

        Assert.assertEquals(-10.0, coordinates[0].getX(), 0.0001);
        Assert.assertEquals(10.0, coordinates[0].getY(), 0.0001);
        Assert.assertEquals(10.0, coordinates[1].getX(), 0.0001);
        Assert.assertEquals(10.0, coordinates[1].getY(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[2].getX(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[2].getY(), 0.0001);
        Assert.assertEquals(10.0, coordinates[3].getX(), 0.0001);
        Assert.assertEquals(-10.0, coordinates[3].getY(), 0.0001);
        Assert.assertEquals(0.0, coordinates[4].getX(), 0.0001);
        Assert.assertEquals(0.0, coordinates[4].getY(), 0.0001);
    }

    @Test
    public void defaultStyle() {
        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.StyleReference style = feature.getStyle();
        Assert.assertEquals("hsl(214, 100%, 48%)",
                style.getStroke().getColor());
        Assert.assertEquals(2, style.getStroke().getWidth());
        Assert.assertFalse(style.getFill().exists());
        Assert.assertFalse(style.getImage().exists());
        Assert.assertFalse(style.getText().exists());
    }

    @Test
    public void customStyle() {
        updateStyle.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.StyleReference style = feature.getStyle();
        Assert.assertEquals("red", style.getStroke().getColor());
        Assert.assertEquals(3, style.getStroke().getWidth());
        Assert.assertFalse(style.getFill().exists());
        Assert.assertFalse(style.getImage().exists());
        Assert.assertFalse(style.getText().exists());
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
