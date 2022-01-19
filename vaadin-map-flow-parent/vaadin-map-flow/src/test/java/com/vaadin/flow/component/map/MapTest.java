package com.vaadin.flow.component.map;

import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import org.junit.Assert;
import org.junit.Test;

public class MapTest {
    @Test
    public void defaultLayer() {
        Map map = new Map();

        Assert.assertEquals(1, map.getConfiguration().getLayers().size());
        Assert.assertTrue(
                map.getConfiguration().getLayers().get(0) instanceof TileLayer);
        Assert.assertTrue(
                ((TileLayer) map.getConfiguration().getLayers().get(0))
                        .getSource() instanceof OSMSource);
    }

    @Test
    public void setBackgroundLayer_replacesDefaultLayer() {
        Map map = new Map();
        TileLayer newBackgroundLayer = new TileLayer();
        map.setBackgroundLayer(newBackgroundLayer);

        Assert.assertEquals(1, map.getConfiguration().getLayers().size());
        Assert.assertTrue(map.getConfiguration().getLayers()
                .contains(newBackgroundLayer));
    }

    @Test
    public void setBackgroundLayer_prependsLayer() {
        Map map = new Map();
        VectorLayer overlayLayer = new VectorLayer();
        map.addLayer(overlayLayer);
        TileLayer newBackgroundLayer = new TileLayer();
        map.setBackgroundLayer(newBackgroundLayer);

        Assert.assertEquals(2, map.getConfiguration().getLayers().size());
        Assert.assertEquals(0,
                map.getConfiguration().getLayers().indexOf(newBackgroundLayer));
        Assert.assertEquals(1,
                map.getConfiguration().getLayers().indexOf(overlayLayer));
    }

    @Test
    public void setBackgroundLayer_doesNotAcceptNull() {
        Map map = new Map();

        Assert.assertThrows(NullPointerException.class,
                () -> map.setBackgroundLayer(null));
    }
}
