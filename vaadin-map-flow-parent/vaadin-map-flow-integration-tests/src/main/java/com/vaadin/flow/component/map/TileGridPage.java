package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.TileWMSSource;
import com.vaadin.flow.component.map.configuration.tilegrid.TileGrid;
import com.vaadin.flow.component.map.configuration.tilegrid.TileSize;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

@Route("vaadin-map/tile-grid")
public class TileGridPage extends Div {

    public TileGridPage() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        add(map);

        // prepare tile grid
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
                .setUrl("https://www.example.com").setParams(params)
                .setTileGrid(tileGrid).setServerType("geoserver"));

        // add layer with custom tile grid to the map
        layer.setSource(source);
        map.addLayer(layer);
    }
}
