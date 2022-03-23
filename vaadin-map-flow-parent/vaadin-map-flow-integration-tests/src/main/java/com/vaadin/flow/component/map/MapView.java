package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.UrlTileSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.router.Route;

import java.util.concurrent.ThreadLocalRandom;

@Route("vaadin-map/map-view")
public class MapView extends Div {
    Span numMarkers = new Span();

    public MapView() {
        Map map = new Map();

        MarkerFeature nurembergMarker = new MarkerFeature(
                new Coordinate(1233058.1696443919, 6351912.406929109));
        map.getFeatureLayer().addFeature(nurembergMarker);

        map.addFeatureClickListener(e -> {
            System.out.println(
                    "Feature click: featureId=" + e.getFeature().getId()
                            + ", layerId=" + e.getLayer().getId());
        });

        NativeButton toggleLayerVisible = new NativeButton("Toggle Layer",
                e -> {
                    Layer layer = map.getBackgroundLayer();
                    layer.setVisible(!layer.isVisible());
                });

        NativeButton useOpenCycleMap = new NativeButton("Use OpenCycleMap",
                e -> {
                    OSMSource.Options options = new OSMSource.Options();
                    options.setUrl(
                            "https://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=187baf2db9fc454896c700ef9e87f499");
                    OSMSource source = new OSMSource(options);
                    TileLayer layer = new TileLayer();
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });

        NativeButton useOpenStreetMap = new NativeButton("Use OpenStreetMap",
                e -> {
                    TileLayer layer = (TileLayer) map.getBackgroundLayer();
                    ((UrlTileSource) layer.getSource()).setUrl(
                            "https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png");
                });

        NativeButton addSeaMapLayer = new NativeButton("Add OpenSeaMap layer",
                e -> {
                    OSMSource.Options options = new OSMSource.Options();
                    options.setUrl(
                            "https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png");
                    OSMSource seaMapSource = new OSMSource(options);
                    TileLayer seaMapLayer = new TileLayer();
                    seaMapLayer.setSource(seaMapSource);
                    map.addLayer(seaMapLayer);
                });

        NativeButton showNuremberg = new NativeButton("Show Nuremberg", e -> {
            map.getView().setCenter(
                    new Coordinate(1233058.1696443919, 6351912.406929109));
            map.getView().setZoom(10);
        });

        NativeButton showSaintNazaire = new NativeButton("Show Saint Nazaire",
                e -> {
                    map.getView().setCenter(new Coordinate(-244780.24508882355,
                            5986452.183179816));
                    map.getView().setZoom(15);
                });

        NativeButton addRandomMarkers = new NativeButton("Add random markers",
                e -> {
                    createRandomMarkers(map.getFeatureLayer(), 100);
                    updateNumMarkers(map.getFeatureLayer().getSource());
                });

        add(map);
        add(new Div(toggleLayerVisible, useOpenCycleMap, useOpenStreetMap,
                addSeaMapLayer, addRandomMarkers, showNuremberg,
                showSaintNazaire));

        add(new Div(numMarkers));
    }

    private void updateNumMarkers(VectorSource vectorSource) {
        numMarkers.setText(
                "Number of markers: " + vectorSource.getFeatures().size());
    }

    private void createRandomMarkers(FeatureLayer layer, int count) {
        for (int i = 0; i < count; i++) {
            double x = ThreadLocalRandom.current().nextDouble(-20026376.39, // NOSONAR
                    20026376.39);
            double y = ThreadLocalRandom.current().nextDouble(-20048966.10, // NOSONAR
                    20048966.10);

            MarkerFeature markerFeature = new MarkerFeature(
                    new Coordinate(x, y));
            layer.addFeature(markerFeature);
        }
    }
}
