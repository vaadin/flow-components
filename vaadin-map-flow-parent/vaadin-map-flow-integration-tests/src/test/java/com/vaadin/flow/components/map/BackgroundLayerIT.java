package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/background-layer")
public class BackgroundLayerIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void defaults() {
        MapElement map = $(MapElement.class).first();

        // Initialized with one layer by default
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(1, numLayers);

        // Layer should be a tile layer
        String layerTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression("map.getLayers().item(0)"));
        Assert.assertEquals("TileLayer", layerTypeName);

        // Layer's source should be an OpenStreetMap source
        String sourceTypeName = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(
                        "map.getLayers().item(0).getSource()"));
        Assert.assertEquals("OSM", sourceTypeName);
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
        Assert.assertEquals("OSM", sourceTypeName);

        // Layer's source should use custom URL
        String sourceUrl = (String) map.evaluateOLExpression(
                "map.getLayers().item(0).getSource().getUrls()[0]");
        Assert.assertEquals("https://example.com", sourceUrl);
    }

    @Test
    public void replaceLayer() {
        MapElement map = $(MapElement.class).first();
        TestBenchElement replaceBackgroundLayer = $("button")
                .id("replace-background-layer");

        replaceBackgroundLayer.click();

        // Should still have a layer
        long numLayers = (long) map
                .evaluateOLExpression("map.getLayers().getLength()");
        Assert.assertEquals(1, numLayers);

        // Layer should be a vector layer
        String layerTypeName = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression("map.getLayers().item(0)"));
        Assert.assertEquals("VectorLayer", layerTypeName);

        // Layer's source should be a vector source
        String sourceTypeName = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(
                        "map.getLayers().item(0).getSource()"));
        Assert.assertEquals("VectorSource", sourceTypeName);
    }
}
