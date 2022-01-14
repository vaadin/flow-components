package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/map-view")
public class MapView extends Div {
    public MapView() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton toggleLayerVisible = new NativeButton("Toggle Layer", e -> {
            Layer layer = map.getBaseLayer();
            layer.setVisible(!layer.isVisible());
        });

        NativeButton useOpenCycleMap = new NativeButton("Use OpenCycleMap", e -> {
            OSMSource source = new OSMSource(new OSMSource.Options().setUrl("https://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=187baf2db9fc454896c700ef9e87f499"));
            TileLayer layer = new TileLayer();
            layer.setSource(source);
            map.setBaseLayer(layer);
        });

        NativeButton useOpenStreetMap = new NativeButton("Use OpenStreetMap", e -> {
            TileLayer layer = (TileLayer) map.getBaseLayer();
            layer.getSource().setUrl("https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png");
        });

        NativeButton addSeaMapLayer = new NativeButton("Add OpenSeaMap layer", e -> {
            OSMSource seaMapSource = new OSMSource(
                    new OSMSource.Options()
                            .setUrl("https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png")
                            .setOpaque(false));
            TileLayer seaMapLayer = new TileLayer();
            seaMapLayer.setSource(seaMapSource);
            map.addLayer(seaMapLayer);
        });

        NativeButton showNuremberg = new NativeButton("Show Nuremberg", e -> {
            map.getView().setCenter(new Coordinate(1233058.1696443919, 6351912.406929109));
            map.getView().setZoom(10);
        });

        NativeButton showSaintNazaire = new NativeButton("Show Saint Nazaire", e -> {
            map.getView().setCenter(new Coordinate(-244780.24508882355, 5986452.183179816));
            map.getView().setZoom(15);
        });

        add(map);
        add(new Div(
                toggleLayerVisible,
                useOpenCycleMap,
                useOpenStreetMap,
                addSeaMapLayer,
                showNuremberg,
                showSaintNazaire
        ));
    }
}
