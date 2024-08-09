/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/marker-feature")
public class MarkerFeaturePage extends Div {
    public MarkerFeaturePage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        NativeButton addDefaultMarkerFeature = new NativeButton(
                "Add default marker feature", e -> {
                    MarkerFeature feature = new MarkerFeature();
                    map.getFeatureLayer().addFeature(feature);
                });
        addDefaultMarkerFeature.setId("add-default-marker-feature");

        NativeButton addCustomMarkerFeature = new NativeButton(
                "Add custom marker feature", e -> {
                    Coordinate coordinate = new Coordinate(11.07675, 49.45203);
                    Icon icon = createCustomIcon();
                    MarkerFeature feature = new MarkerFeature(coordinate, icon);
                    map.getFeatureLayer().addFeature(feature);
                });
        addCustomMarkerFeature.setId("add-custom-marker-feature");

        NativeButton updateMarkerCoordinates = new NativeButton(
                "Update marker coordinates", e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        MarkerFeature feature = (MarkerFeature) map
                                .getFeatureLayer().getFeatures().get(0);
                        Coordinate coordinate = new Coordinate(11.07675,
                                49.45203);
                        feature.setCoordinates(coordinate);
                    }
                });
        updateMarkerCoordinates.setId("update-marker-coordinates");

        NativeButton updateMarkerIcon = new NativeButton("Update marker icon",
                e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        MarkerFeature feature = (MarkerFeature) map
                                .getFeatureLayer().getFeatures().get(0);
                        Icon icon = createCustomIcon();
                        feature.setIcon(icon);
                    }
                });
        updateMarkerIcon.setId("update-marker-icon");

        add(map);
        add(new Div(addDefaultMarkerFeature, addCustomMarkerFeature,
                updateMarkerCoordinates, updateMarkerIcon));
    }

    private static Icon createCustomIcon() {
        Icon.Options options = new Icon.Options();
        options.setSrc("assets/custom-marker.png");
        options.setColor("blue");
        options.setOpacity(0.8);
        options.setScale(2);
        options.setRotation(Math.PI);
        return new Icon(options);
    }
}
