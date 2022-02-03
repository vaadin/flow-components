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

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setCustomSource = $("button").id("set-custom-source");
        replaceBackgroundLayer = $("button").id("replace-background-layer");
        addCustomLayer = $("button").id("add-custom-layer");
        removeCustomLayer = $("button").id("remove-custom-layer");
    }

    @Test
    public void defaultLayers() {
        // Initialized with two layers by default
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);

        // First layer should be a tile layer
        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String backgroundLayerType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(backgroundLayerEx));
        Assert.assertEquals("ol/layer/Tile", backgroundLayerType);

        // Layer's source should be an OpenStreetMap source
        String backgroundLayerSourceEx = backgroundLayerEx + ".getSource()";
        String sourceTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(backgroundLayerSourceEx));
        Assert.assertEquals("ol/source/OSM", sourceTypeName);

        // Second layer should be a vector layer
        String featureLayerEx = map.getLayerExpression("feature-layer");
        String featureLayerType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(featureLayerEx));
        Assert.assertEquals("ol/layer/Vector", featureLayerType);
    }

    @Test
    public void backgroundLayer_setCustomSource() {
        setCustomSource.click();

        // Background layer's source should now be an XYZ source
        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String backgroundLayerSourceEx = backgroundLayerEx + ".getSource()";
        String sourceTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(backgroundLayerSourceEx));
        Assert.assertEquals("ol/source/XYZ", sourceTypeName);

        // Layer's source should use custom URL
        String sourceUrl = (String) map.evaluateOLExpression(
                backgroundLayerSourceEx + ".getUrls()[0]");
        Assert.assertEquals("https://example.com", sourceUrl);
    }

    @Test
    public void backgroundLayer_replace() {
        replaceBackgroundLayer.click();

        // Should still have two layers
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);

        // Layer should be a vector layer
        String newBackgroundLayerEx = map
                .getLayerExpression("new-background-layer");
        String layerTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(newBackgroundLayerEx));
        Assert.assertEquals("ol/layer/Vector", layerTypeName);

        // Layer's source should be a vector source
        String newBackgroundLayerSourceEx = newBackgroundLayerEx
                + ".getSource()";
        String sourceTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(newBackgroundLayerSourceEx));
        Assert.assertEquals("ol/source/Vector", sourceTypeName);
    }

    @Test
    public void addCustomLayer() {
        addCustomLayer.click();

        // Should still have three layers
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(3, numLayers);

        String customLayerEx = map
                .getLayerExpression("custom-layer");
        Boolean hasCustomLayer = (Boolean) map.evaluateOLExpression(customLayerEx + " != null");

        Assert.assertTrue("Custom layer does not exist", hasCustomLayer);
    }

    @Test
    public void removeCustomLayer() {
        addCustomLayer.click();
        removeCustomLayer.click();

        // Should have two layers
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);

        String customLayerEx = map
                .getLayerExpression("custom-layer");
        Boolean hasCustomLayer = (Boolean) map.evaluateOLExpression(customLayerEx + " != null");

        Assert.assertFalse("Custom layer still exists", hasCustomLayer);
    }
}
