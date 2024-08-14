/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

/**
 * Demo that displays a tile images from an XYZ source
 */
@Route("vaadin-map/demo/xyz-source")
public class XYZSourceDemo extends Div {
    public XYZSourceDemo() {
        Map map = new Map();
        add(map);

        // Use XYZ source displaying the OSM humanitarian map
        String url = "https://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png";
        XYZSource.Options options = new XYZSource.Options();
        options.setUrl(url);
        XYZSource xyzSource = new XYZSource(options);

        TileLayer tileLayer = new TileLayer();
        tileLayer.setSource(xyzSource);

        map.setBackgroundLayer(tileLayer);

        // Move viewport to Cap Haitien
        map.getView().setCenter(new Coordinate(-72.2, 19.76));
        map.getView().setZoom(12);
    }
}
