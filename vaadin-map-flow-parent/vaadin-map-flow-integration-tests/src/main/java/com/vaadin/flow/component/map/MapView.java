package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/map-view")
public class MapView extends Div {
    public MapView() {
        Map map = new Map();
        Configuration configuration = map.getConfiguration();

        OSMSource source = new OSMSource();
        TileLayer layer = new TileLayer();
        layer.setSource(source);
        configuration.addLayer(layer);

        add(map);
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton toggleLayerVisible = new NativeButton("Toggle Layer", e -> {
            layer.setVisible(!layer.isVisible());
        });

        NativeButton showNuremberg = new NativeButton("Show Nuremberg", e -> {
            map.getView().setCenter(new Coordinate(1233058.1696443919, 6351912.406929109));
            map.getView().setZoom(10);
        });

        add(new Div(
                toggleLayerVisible,
                showNuremberg
        ));
    }
}
