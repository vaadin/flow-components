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

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setupTileWMSSource = $("button").id("setup-tile-wms-source");
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
}
