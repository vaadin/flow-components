package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.TileWMSSource;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

/**
 * Demo that displays a tile WMS layer on top of the default OSM background
 * layer
 * <p>
 * Adapted from https://openlayers.org/en/latest/examples/wms-tiled.html
 */
@Route("vaadin-map/demo/tile-wms")
public class TileWMSDemo extends Div {
    public TileWMSDemo() {
        Map map = new Map();
        add(map);

        // Add tile layer showing an overlay of the individual states of the US
        HashMap<String, Object> params = new HashMap<>();
        params.put("LAYERS", "topp:states");
        params.put("TILED", true);
        TileWMSSource tileWMSSource = new TileWMSSource(
                new TileWMSSource.Options()
                        .setUrl("https://ahocevar.com/geoserver/wms")
                        .setParams(params).setServerType("geoserver"));

        TileLayer tileLayer = new TileLayer();
        tileLayer.setSource(tileWMSSource);

        map.addLayer(tileLayer);

        // Move viewport to US
        map.getView().setCenter(new Coordinate(-10997148, 4569099));
        map.getView().setZoom(4);
    }
}
