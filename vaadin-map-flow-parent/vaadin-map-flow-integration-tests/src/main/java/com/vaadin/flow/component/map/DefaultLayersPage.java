package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/default-layers")
public class DefaultLayersPage extends Div {
    public DefaultLayersPage() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton setCustomOsmSource = new NativeButton(
                "Set custom OSM source", e -> {
                    TileLayer backgroundLayer = (TileLayer) map
                            .getBackgroundLayer();
                    backgroundLayer
                            .setSource(new OSMSource(new OSMSource.Options()
                                    .setUrl("https://example.com")));
                });
        setCustomOsmSource.setId("set-custom-osm-source");

        NativeButton replaceBackgroundLayer = new NativeButton(
                "Replace background layer", e -> {
                    VectorSource vectorSource = new VectorSource();
                    VectorLayer vectorLayer = new VectorLayer();
                    vectorLayer.setSource(vectorSource);
                    map.setBackgroundLayer(vectorLayer);
                });
        replaceBackgroundLayer.setId("replace-background-layer");

        add(map);
        add(new Div(setCustomOsmSource, replaceBackgroundLayer));
    }
}
