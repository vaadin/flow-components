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
        map.setWidthFull();
        map.setHeight("400px");
        add(map);

        // Add image layer showing an overlay of the individual states of the US
        HashMap<String, Object> params = new HashMap<>();
        params.put("LAYERS", "topp:states");
        ImageWMSSource imageWMSSource = new ImageWMSSource(
                new ImageWMSSource.Options()
                        .setUrl("https://ahocevar.com/geoserver/wms")
                        .setParams(params).setServerType("geoserver")
                        .setRatio(1));

        ImageLayer imageLayer = new ImageLayer();
        imageLayer.setSource(imageWMSSource);

        map.addLayer(imageLayer);

        // Move viewport to US
        map.getView().setCenter(new Coordinate(-10997148, 4569099));
        map.getView().setZoom(4);
    }
}
