package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.TileWMSSource;
import com.vaadin.flow.component.map.configuration.tilegrid.Extent;
import com.vaadin.flow.component.map.configuration.tilegrid.TileGrid;
import com.vaadin.flow.component.map.configuration.tilegrid.TileSize;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

@Route("vaadin-map/demo/tile-grid")
public class TileGridDemo extends Div {
    public TileGridDemo() {
        Map map = new Map();
        map.setHeight("400px");
        map.setWidthFull();
        map.getView().setCenter(new Coordinate(-10997148, 4569099));
        map.getView().setZoom(4);
        add(map);

        int RADIUS = 6378137;
        double HALF_SIZE = Math.PI * RADIUS;
        Extent extent = new Extent(-HALF_SIZE, -HALF_SIZE, HALF_SIZE,
                HALF_SIZE);
        double startResolution = extent.getWidth() / 256;
        double[] resolutions = new double[22];
        for (int i = 0; i < resolutions.length; ++i) {
            resolutions[i] = startResolution / Math.pow(2, i);
        }
        TileLayer layer = new TileLayer();
        TileGrid tileGrid = new TileGrid(extent, new TileSize(512, 256),
                resolutions);

        HashMap<String, Object> params = new HashMap<>();
        params.put("LAYERS", "topp:states");
        params.put("TILED", true);

        TileWMSSource source = new TileWMSSource(new TileWMSSource.Options()
                .setUrl("https://ahocevar.com/geoserver/wms").setParams(params)
                .setTileGrid(tileGrid).setServerType("geoserver"));

        layer.setSource(source);

        map.addLayer(layer);
    }
}
