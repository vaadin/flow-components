package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for testing the synchronization of properties for different types
 * of sources
 */
@TestPath("vaadin-map/sources")
public class SourcesIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement setupTileWMSSource;
    private TestBenchElement setupXYZSource;
    private TestBenchElement setupImageWMSSource;
    private TestBenchElement setupTileJSONSource;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setupTileJSONSource = $("button").id("setup-tile-json-source");
        setupTileWMSSource = $("button").id("setup-tile-wms-source");
        setupXYZSource = $("button").id("setup-xyz-source");
        setupImageWMSSource = $("button").id("setup-image-wms-source");
    }

    @Test
    public void initializeTileJSONSource() {
        setupTileJSONSource.click();

        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String sourceEx = backgroundLayerEx + ".getSource()";

        String sourceType = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(sourceEx));
        String url = (String) map
                .evaluateOLExpression(sourceEx + ".getUrls()[0]");

        Assert.assertEquals("ol/source/TileJSON", sourceType);
        Assert.assertEquals("https://example.com/tilejson", url);
    }

    @Test
    public void initializeTileWMSSource() {
        setupTileWMSSource.click();

        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String sourceEx = backgroundLayerEx + ".getSource()";

        String sourceType = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(sourceEx));
        String url = (String) map
                .evaluateOLExpression(sourceEx + ".getUrls()[0]");
        String layersParam = (String) map
                .evaluateOLExpression(sourceEx + ".params_.LAYERS");
        Boolean tiledParam = (Boolean) map
                .evaluateOLExpression(sourceEx + ".params_.TILED");
        String serverType = (String) map
                .evaluateOLExpression(sourceEx + ".serverType_");

        Assert.assertEquals("ol/source/TileWMS", sourceType);
        Assert.assertEquals("https://example.com/wms", url);
        Assert.assertEquals("layer1", layersParam);
        Assert.assertEquals(true, tiledParam);
        Assert.assertEquals("geoserver", serverType);
    }

    @Test
    public void initializeXYZSource() {
        setupXYZSource.click();

        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String sourceEx = backgroundLayerEx + ".getSource()";

        String sourceType = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(sourceEx));
        String url = (String) map
                .evaluateOLExpression(sourceEx + ".getUrls()[0]");

        Assert.assertEquals("ol/source/XYZ", sourceType);
        Assert.assertEquals("https://example.com/wms", url);
    }

    @Test
    public void initializeImageWMSSource() {
        setupImageWMSSource.click();

        String backgroundLayerEx = map.getLayerExpression("background-layer");
        String sourceEx = backgroundLayerEx + ".getSource()";

        String sourceType = (String) map
                .evaluateOLExpression(map.getOLTypeNameExpression(sourceEx));
        String url = (String) map.evaluateOLExpression(sourceEx + ".url_");
        String layersParam = (String) map
                .evaluateOLExpression(sourceEx + ".params_.LAYERS");
        String serverType = (String) map
                .evaluateOLExpression(sourceEx + ".serverType_");
        String crossOrigin = (String) map
                .evaluateOLExpression(sourceEx + ".crossOrigin_");
        float ratio = ((Number) map.evaluateOLExpression(sourceEx + ".ratio_"))
                .floatValue();

        Assert.assertEquals("ol/source/ImageWMS", sourceType);
        Assert.assertEquals("https://example.com/wms", url);
        Assert.assertEquals("layer1", layersParam);
        Assert.assertEquals("geoserver", serverType);
        Assert.assertEquals("custom-cross-origin", crossOrigin);
        Assert.assertEquals(2, ratio, 0.1);
    }
}
