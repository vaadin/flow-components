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
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/polygon-feature")
public class PolygonFeaturePage extends Div {

    public PolygonFeaturePage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        NativeButton addSimplePolygonFeature = new NativeButton(
                "Add simple polygon feature", e -> {
                    List<Coordinate> coordinates = createOuterBoundaries();
                    PolygonFeature polygon = new PolygonFeature(coordinates);

                    map.getFeatureLayer().addFeature(polygon);
                });
        addSimplePolygonFeature.setId("add-simple-polygon-feature");

        NativeButton addPolygonFeatureWithHole = new NativeButton(
                "Add polygon feature with a hole", e -> {
                    List<Coordinate> outerBoundary = createOuterBoundaries();
                    List<Coordinate> hole = List.of(new Coordinate(6, 48), // SW
                            new Coordinate(6, 54), // NW
                            new Coordinate(14, 54), // NE
                            new Coordinate(14, 48), // SE
                            new Coordinate(6, 48) // SW
                    );

                    Coordinate[][] coordinates = new Coordinate[][] {
                            outerBoundary.toArray(new Coordinate[0]),
                            hole.toArray(new Coordinate[0]) };
                    PolygonFeature polygon = new PolygonFeature();
                    polygon.setCoordinates(coordinates);

                    map.getFeatureLayer().addFeature(polygon);

                });
        addPolygonFeatureWithHole.setId("add-polygon-feature-with-hole");

        NativeButton movePolygonFeature = new NativeButton(
                "Move polygon feature", e -> {
                    if (!map.getFeatureLayer().getFeatures().isEmpty()) {
                        Feature feature = map.getFeatureLayer().getFeatures()
                                .get(0);
                        if (feature instanceof PolygonFeature polygonFeature) {
                            polygonFeature.getGeometry().translate(5.0, -3.0);
                        }
                    }
                });
        movePolygonFeature.setId("move-polygon-feature");

        NativeButton activateDragAndDrop = new NativeButton(
                "Activate Drag & Drop", e -> map.getFeatureLayer().getFeatures()
                        .forEach(feature -> feature.setDraggable(true)));

        NativeButton updatePolygonStyle = new NativeButton(
                "Update polygon style", e -> {
                    if (!map.getFeatureLayer().getFeatures().isEmpty()) {
                        Feature feature = map.getFeatureLayer().getFeatures()
                                .get(0);
                        Style style = new Style();
                        style.setStroke(new Stroke("red", 3));
                        style.setFill(new Fill("rgba(255, 0, 0, 0.1)"));
                        feature.setStyle(style);
                    }
                });

        NativeButton removePolygons = new NativeButton("Remove polygons",
                e -> map.getFeatureLayer().removeAllFeatures());

        add(map);
        add(new Div(addSimplePolygonFeature, addPolygonFeatureWithHole,
                movePolygonFeature, activateDragAndDrop, updatePolygonStyle,
                removePolygons));
    }

    private static List<Coordinate> createOuterBoundaries() {
        return List.of(new Coordinate(5, 47), // SW
                new Coordinate(5, 55), // NW
                new Coordinate(15, 55), // NE
                new Coordinate(15, 47), // SE
                new Coordinate(5, 47) // SW
        );
    }
}
