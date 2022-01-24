package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/default-layers")
public class DefaultLayers extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void defaults() {
        MapElement map = $(MapElement.class).first();

        // Initialized with two layers by default
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);

        // First layer should be a tile layer
        String backgroundLayerType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression("map.getLayers().item(0)"));
        Assert.assertEquals("ol/layer/Tile", backgroundLayerType);

        // Layer's source should be an OpenStreetMap source
        String sourceTypeName = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(
                        "map.getLayers().item(0).getSource()"));
        Assert.assertEquals("ol/source/OSM", sourceTypeName);

        // Second layer should be a vector layer
        String featureLayerType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression("map.getLayers().item(1)"));
        Assert.assertEquals("ol/layer/Vector", featureLayerType);
    }

    @Test
    public void customOsmSource() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement setCustomOsmSource = $("button")
                .id("set-custom-osm-source");

        setCustomOsmSource.click();

        // Layer's source should be an OpenStreetMap source
        String sourceTypeName = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(
                        "map.getLayers().item(0).getSource()"));
        Assert.assertEquals("ol/source/OSM", sourceTypeName);

        // Layer's source should use custom URL
        String sourceUrl = (String) map.evaluateOLExpression(
                "map.getLayers().item(0).getSource().getUrls()[0]");
        Assert.assertEquals("https://example.com", sourceUrl);
    }

    @Test
    public void customBackgroundLayer() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement replaceBackgroundLayer = $("button")
                .id("replace-background-layer");

        replaceBackgroundLayer.click();

        // Should still have two layers
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(2, numLayers);

        // Layer should be a vector layer
        String layerTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression("map.getLayers().item(0)"));
        Assert.assertEquals("ol/layer/Vector", layerTypeName);

        // Layer's source should be a vector source
        String sourceTypeName = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(
                        "map.getLayers().item(0).getSource()"));
        Assert.assertEquals("ol/source/Vector", sourceTypeName);
    }
}
