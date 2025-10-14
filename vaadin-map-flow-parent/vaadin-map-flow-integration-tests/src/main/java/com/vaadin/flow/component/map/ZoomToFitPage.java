/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/zoom-to-fit")
public class ZoomToFitPage extends Div {
    public static final MarkerFeature FEATURE_1 = new MarkerFeature(
            new Coordinate(-40, 0));
    public static final MarkerFeature FEATURE_2 = new MarkerFeature(
            new Coordinate(-41, 0));
    public static final MarkerFeature FEATURE_3 = new MarkerFeature(
            new Coordinate(-42, 0));
    public static final MarkerFeature FEATURE_4 = new MarkerFeature(
            new Coordinate(40, 0));
    public static final MarkerFeature FEATURE_5 = new MarkerFeature(
            new Coordinate(41, 0));
    public static final MarkerFeature FEATURE_6 = new MarkerFeature(
            new Coordinate(42, 0));

    public ZoomToFitPage() {
        Map map = new Map();

        map.getFeatureLayer().addFeature(FEATURE_1);
        map.getFeatureLayer().addFeature(FEATURE_2);
        map.getFeatureLayer().addFeature(FEATURE_3);

        map.getFeatureLayer().addFeature(FEATURE_4);
        map.getFeatureLayer().addFeature(FEATURE_5);
        map.getFeatureLayer().addFeature(FEATURE_6);

        // Initial zoom to fit
        map.zoomToFit(List.of(FEATURE_1, FEATURE_2, FEATURE_3), 50, 0);

        // Buttons to fit individual sets
        NativeButton zoomToFirstSet = new NativeButton("Zoom to fit first set",
                e -> map.zoomToFit(List.of(FEATURE_1, FEATURE_2, FEATURE_3), 50,
                        0));
        zoomToFirstSet.setId("zoom-to-first-set");

        NativeButton zoomToSecondSet = new NativeButton(
                "Zoom to fit second set",
                e -> map.zoomToFit(List.of(FEATURE_4, FEATURE_5, FEATURE_6), 50,
                        0));
        zoomToSecondSet.setId("zoom-to-second-set");

        add(map, zoomToFirstSet, zoomToSecondSet);
    }
}
