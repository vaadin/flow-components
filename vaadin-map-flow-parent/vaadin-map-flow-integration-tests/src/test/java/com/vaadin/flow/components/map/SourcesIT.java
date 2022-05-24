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

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        setupTileWMSSource = $("button").id("setup-tile-wms-source");
        setupXYZSource = $("button").id("setup-xyz-source");
        setupImageWMSSource = $("button").id("setup-image-wms-source");
    }

    @Test
    public void initializeTileWMSSource() {
        setupTileWMSSource.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers().getLayer(0);
        MapElement.TileWmsSourceReference source = layer.getSource()
                .asTileWmsSource();

        Assert.assertEquals("ol/source/TileWMS", source.getTypeName());
        Assert.assertEquals("https://example.com/wms", source.getPrimaryUrl());
        Assert.assertEquals("layer1", source.getParam("LAYERS"));
        Assert.assertEquals(true, source.getParam("TILED"));
        Assert.assertEquals("geoserver", source.getServerType());
    }

    @Test
    public void initializeXYZSource() {
        setupXYZSource.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers().getLayer(0);
        MapElement.XyzSourceReference source = layer.getSource().asXyzSource();

        Assert.assertEquals("ol/source/XYZ", source.getTypeName());
        Assert.assertEquals("https://example.com/wms", source.getPrimaryUrl());
    }

    @Test
    public void initializeImageWMSSource() {
        setupImageWMSSource.click();

        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference layer = mapReference.getLayers().getLayer(0);
        MapElement.ImageWmsSourceReference source = layer.getSource()
                .asImageWmsSource();

        Assert.assertEquals("ol/source/ImageWMS", source.getTypeName());
        Assert.assertEquals("https://example.com/wms", source.getUrl());
        Assert.assertEquals("layer1", source.getParam("LAYERS"));
        Assert.assertEquals("geoserver", source.getServerType());
        Assert.assertEquals("custom-cross-origin", source.getCrossOrigin());
        Assert.assertEquals(2, source.getRatio(), 0.1);
    }
}
