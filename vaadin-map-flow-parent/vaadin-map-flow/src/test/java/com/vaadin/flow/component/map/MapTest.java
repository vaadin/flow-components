package com.vaadin.flow.component.map;

import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import org.junit.Assert;
import org.junit.Test;

public class MapTest {
    @Test
    public void defaults() {
        Map map = new Map();

        Assert.assertEquals(2, map.getConfiguration().getLayers().size());
        Layer backgroundLayer = map.getConfiguration().getLayers().get(0);
        Layer featureLayer = map.getConfiguration().getLayers().get(1);

        Assert.assertTrue(backgroundLayer instanceof TileLayer);
        Assert.assertTrue(
                ((TileLayer) backgroundLayer).getSource() instanceof OSMSource);
        Assert.assertTrue(featureLayer instanceof FeatureLayer);
    }

    @Test
    public void setBackgroundLayer_replacesDefaultLayer() {
        Map map = new Map();
        TileLayer newBackgroundLayer = new TileLayer();
        map.setBackgroundLayer(newBackgroundLayer);

        Assert.assertEquals(2, map.getConfiguration().getLayers().size());
        Assert.assertTrue(map.getConfiguration().getLayers()
                .contains(newBackgroundLayer));
    }

    @Test
    public void setBackgroundLayer_prependsLayer() {
        Map map = new Map();
        TileLayer newBackgroundLayer = new TileLayer();
        map.setBackgroundLayer(newBackgroundLayer);

        Assert.assertEquals(2, map.getConfiguration().getLayers().size());
        Assert.assertEquals(0,
                map.getConfiguration().getLayers().indexOf(newBackgroundLayer));
    }

    @Test
    public void setBackgroundLayer_doesNotAcceptNull() {
        Map map = new Map();

        Assert.assertThrows(NullPointerException.class,
                () -> map.setBackgroundLayer(null));
    }
}
