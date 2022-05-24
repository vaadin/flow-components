package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.ImageLayer;
import com.vaadin.flow.component.map.configuration.source.ImageWMSSource;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

/**
 * Demo that displays an image WMS layer on top of the default OSM background
 * layer
 * <p>
 * Adapted from https://openlayers.org/en/latest/examples/wms-tiled.html
 */
@Route("vaadin-map/demo/image-wms")
public class ImageWMSDemo extends Div {
    public ImageWMSDemo() {
        Map map = new Map();
        add(map);

        // Add image layer showing an overlay of the individual states of the US
        HashMap<String, Object> params = new HashMap<>();
        params.put("LAYERS", "topp:states");
        ImageWMSSource.Options options = new ImageWMSSource.Options();
        options.setUrl("https://ahocevar.com/geoserver/wms");
        options.setParams(params);
        options.setServerType("geoserver");
        options.setRatio(1);
        ImageWMSSource imageWMSSource = new ImageWMSSource(options);

        ImageLayer imageLayer = new ImageLayer();
        imageLayer.setSource(imageWMSSource);

        map.addLayer(imageLayer);

        // Move viewport to US
        map.getView().setCenter(new Coordinate(-10997148, 4569099));
        map.getView().setZoom(4);
    }
}
