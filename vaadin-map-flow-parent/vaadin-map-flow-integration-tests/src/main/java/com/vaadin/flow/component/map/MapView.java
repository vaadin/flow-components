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
        layer.setId("layer1");
        layer.setSource(source);

        configuration.getLayers().add(layer);
        configuration.getView().setZoom(3);

        add(map);
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton toggleLayerVisible = new NativeButton("Toggle Layer", e -> {
            layer.setVisible(!layer.isVisible());
            map.render();
        });

        NativeButton showNuremberg = new NativeButton("Show Nuremberg", e -> {
            configuration.getView().setCenter(new Coordinate(1233058.1696443919, 6351912.406929109));
            configuration.getView().setZoom(10);
            map.render();
        });

        add(new Div(
                toggleLayerVisible,
                showNuremberg
        ));
    }
}
