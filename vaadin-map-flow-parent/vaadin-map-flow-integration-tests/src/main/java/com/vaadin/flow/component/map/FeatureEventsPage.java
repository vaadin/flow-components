package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-events")
public class FeatureEventsPage extends Div {
    public FeatureEventsPage() {
        Map map = new Map();
        map.getView().setCenter(new Coordinate(0, 0));
        map.getView().setZoom(3);

        // Setup first feature layer with one marker
        FeatureLayer firstFeatureLayer = new FeatureLayer();
        firstFeatureLayer.setId("first-feature-layer");
        firstFeatureLayer.getSource().setId("first-source");
        map.addLayer(firstFeatureLayer);

        MarkerFeature firstMarkerFeature = new MarkerFeature(
                new Coordinate(0, 0));
        firstMarkerFeature.setId("first-marker-feature");
        firstFeatureLayer.addFeature(firstMarkerFeature);

        // Setup second feature layer with one marker
        FeatureLayer secondFeatureLayer = new FeatureLayer();
        secondFeatureLayer.setId("second-feature-layer");
        secondFeatureLayer.getSource().setId("second-source");
        map.addLayer(secondFeatureLayer);

        MarkerFeature secondMarkerFeature = new MarkerFeature(
                new Coordinate(2000000, 0));
        secondMarkerFeature.setId("second-marker-feature");
        secondFeatureLayer.addFeature(secondMarkerFeature);

        // Setup several overlapping markers to verify that we receive only one
        // event when clicking that location
        int numOverlappingMarkers = 3;
        for (int i = 0; i < numOverlappingMarkers; i++) {
            MarkerFeature overlappingMarker = new MarkerFeature(
                    new Coordinate(4000000, 0));
            overlappingMarker.setId("overlapping-marker-feature-" + (i + 1));
            firstFeatureLayer.addFeature(overlappingMarker);
        }

        Div eventLog = new Div();
        eventLog.setId("event-log");
        eventLog.getElement().getStyle().set("white-space", "pre");

        NativeButton addGlobalFeatureClickListener = new NativeButton(
                "Add global feature click listener", e -> {
                    map.addFeatureClickListener(event -> {
                        Feature feature = event.getFeature();
                        VectorLayer layer = event.getLayer();
                        VectorSource source = event.getVectorSource();
                        String eventInfoText = String.format(
                                "click: feature=%s | layer=%s | source=%s%n",
                                feature.getId(), layer.getId(), source.getId());

                        eventLog.setText(eventLog.getText() + eventInfoText);
                    });
                });
        addGlobalFeatureClickListener
                .setId("add-global-feature-click-listener");

        NativeButton addFirstLayerFeatureClickListener = new NativeButton(
                "Add feature click listener for first layer only", e -> {
                    map.addFeatureClickListener(firstFeatureLayer, event -> {
                        Feature feature = event.getFeature();
                        VectorLayer layer = event.getLayer();
                        VectorSource source = event.getVectorSource();
                        String eventInfoText = String.format(
                                "click: feature=%s | layer=%s | source=%s%n",
                                feature.getId(), layer.getId(), source.getId());

                        eventLog.setText(eventLog.getText() + eventInfoText);
                    });
                });
        addFirstLayerFeatureClickListener
                .setId("add-first-layer-feature-click-listener");

        add(map, new Div(addGlobalFeatureClickListener,
                addFirstLayerFeatureClickListener), eventLog);
    }
}
