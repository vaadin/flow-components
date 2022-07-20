package com.vaadin.flow.component.map.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.Map;
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

@Route("vaadin-map/demo/sampler")
public class SamplerView extends Div {
    Span numMarkers = new Span();

    public SamplerView() {
        Map map = new Map();

        MarkerFeature nurembergMarker = new MarkerFeature(
                new Coordinate(11.07675, 49.45203));
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

        NativeButton useHumanitarianMap = new NativeButton(
                "Use Humanitarian Map", e -> {
                    OSMSource.Options options = new OSMSource.Options();
                    options.setUrl(
                            "https://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png");
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
            map.getView().setCenter(new Coordinate(11.07675, 49.45203));
            map.getView().setZoom(10);
        });

        NativeButton showSaintNazaire = new NativeButton("Show Saint Nazaire",
                e -> {
                    map.getView()
                            .setCenter(new Coordinate(-2.1988983, 47.2711907));
                    map.getView().setZoom(15);
                });

        NativeButton addRandomMarkers = new NativeButton("Add random markers",
                e -> {
                    createRandomMarkers(map.getFeatureLayer(), 100);
                    updateNumMarkers(map.getFeatureLayer().getSource());
                });

        add(map);
        add(new Div(toggleLayerVisible, useHumanitarianMap, useOpenStreetMap,
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
            double x = ThreadLocalRandom.current().nextDouble(-180, // NOSONAR
                    180);
            double y = ThreadLocalRandom.current().nextDouble(-90, // NOSONAR
                    90);

            MarkerFeature markerFeature = new MarkerFeature(
                    new Coordinate(x, y));
            layer.addFeature(markerFeature);
        }
    }
}
