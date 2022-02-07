package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.TileJSONSource;
import com.vaadin.flow.router.Route;

/**
 * Demo that displays a tile images from a TileJSON source
 */
@Route("vaadin-map/demo/tile-json-source")
public class TileJSONSourceDemo extends Div {
    public TileJSONSourceDemo() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");
        add(map);

        // Use TileJSON source displaying a country map from MapBox
        String url = "https://a.tiles.mapbox.com/v3/aj.1x1-degrees.json?secure=1";
        TileJSONSource xyzSource = new TileJSONSource(
                new TileJSONSource.Options().setUrl(url));

        TileLayer tileLayer = new TileLayer();
        tileLayer.setSource(xyzSource);

        map.setBackgroundLayer(tileLayer);
    }
}
