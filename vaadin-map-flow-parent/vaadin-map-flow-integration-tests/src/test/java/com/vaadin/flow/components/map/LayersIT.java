package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/layers")
public class LayersIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement setCustomSource;
    private TestBenchElement replaceBackgroundLayer;
    private TestBenchElement addCustomLayer;
    private TestBenchElement removeCustomLayer;
    private TestBenchElement customizeLayerProperties;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setCustomSource = $("button").id("set-custom-source");
        replaceBackgroundLayer = $("button").id("replace-background-layer");
        addCustomLayer = $("button").id("add-custom-layer");
        removeCustomLayer = $("button").id("remove-custom-layer");
        customizeLayerProperties = $("button").id("customize-layer-properties");
    }

    @Test
    public void defaultLayers() {
        // Initialized with two layers by default
        MapElement.MapReference mapReference = map.getMapReference();
        Assert.assertEquals(2, mapReference.getLayers().getLength());

        // First layer should be a tile layer with an OpenStreetMap source
        MapElement.LayerReference backgroundLayer = mapReference.getLayers()
                .getLayer(0);
        MapElement.SourceReference backgroundSource = backgroundLayer
                .getSource();

        Assert.assertEquals("ol/layer/Tile", backgroundLayer.getTypeName());
        Assert.assertTrue(backgroundLayer.isVisible());
        Assert.assertEquals(1f, backgroundLayer.getOpacity(), 0.001);
        Assert.assertNull(backgroundLayer.getZIndex());
        Assert.assertEquals("ol/source/OSM", backgroundSource.getTypeName());

        // Second layer should be a vector layer with a vector source
        MapElement.LayerReference featureLayer = mapReference.getLayers()
                .getLayer(1);
        MapElement.SourceReference featureLayerSource = featureLayer
                .getSource();

        Assert.assertEquals("ol/layer/Vector", featureLayer.getTypeName());
        Assert.assertTrue(featureLayer.isVisible());
        Assert.assertEquals(1f, featureLayer.getOpacity(), 0.001);
        Assert.assertEquals(100, (long) featureLayer.getZIndex());
        Assert.assertEquals("ol/source/Vector",
                featureLayerSource.getTypeName());
    }

    @Test
    public void backgroundLayer_setCustomSource() {
        setCustomSource.click();

        // Background layer's source should now be an XYZ source
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference backgroundLayer = mapReference.getLayers()
                .getLayer(0);
        MapElement.XyzSourceReference backgroundSource = backgroundLayer
                .getSource().asXyzSource();

        Assert.assertEquals("ol/source/XYZ", backgroundSource.getTypeName());

        // Layer's source should use custom URL
        Assert.assertEquals("https://example.com",
                backgroundSource.getPrimaryUrl());
    }

    @Test
    public void backgroundLayer_replace() {
        replaceBackgroundLayer.click();

        // Should still have two layers
        MapElement.MapReference mapReference = map.getMapReference();
        Assert.assertEquals(2, mapReference.getLayers().getLength());

        // Layer should be a vector layer with a vector source
        MapElement.LayerReference backgroundLayer = mapReference.getLayers()
                .getLayer(0);
        MapElement.SourceReference backgroundSource = backgroundLayer
                .getSource();

        Assert.assertEquals("ol/layer/Vector", backgroundLayer.getTypeName());
        Assert.assertEquals("ol/source/Vector", backgroundSource.getTypeName());
    }

    @Test
    public void addCustomLayer() {
        addCustomLayer.click();

        // Should now have three layers
        MapElement.MapReference mapReference = map.getMapReference();
        Assert.assertEquals(3, mapReference.getLayers().getLength());

        // Custom layer should exist
        MapElement.LayerReference customLayer = mapReference.getLayers()
                .getLayer("custom-layer");
        Assert.assertTrue("Custom layer does not exist", customLayer.exists());
    }

    @Test
    public void removeCustomLayer() {
        addCustomLayer.click();
        removeCustomLayer.click();

        // Should have the two default layers
        MapElement.MapReference mapReference = map.getMapReference();
        Assert.assertEquals(2, mapReference.getLayers().getLength());

        // Custom layer should not exist
        MapElement.LayerReference customLayer = mapReference.getLayers()
                .getLayer("custom-layer");
        Assert.assertFalse("Custom layer still exists", customLayer.exists());
    }

    @Test
    public void customizeProperties() {
        addCustomLayer.click();
        customizeLayerProperties.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference customLayer = mapReference.getLayers()
                .getLayer("custom-layer");
        Assert.assertTrue("Custom layer does not exist", customLayer.exists());

        Assert.assertFalse(customLayer.isVisible());
        Assert.assertEquals(0.7f, customLayer.getOpacity(), 0.0001);
        Assert.assertEquals(42, (long) customLayer.getZIndex());
    }
}
