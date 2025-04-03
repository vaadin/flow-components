/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import java.util.List;

@Route("vaadin-map/polygon-feature")
public class PolygonFeaturePage extends Div {

    public PolygonFeaturePage() {
        Map map = new Map();
        map.setCenter(new Coordinate(10.4515, 51.1657));
        map.setZoom(4);
        map.getFeatureLayer().setId("feature-layer");

        //
        // ----
        NativeButton addSimplePolygonFeature = new NativeButton(
                "Add simple polygon feature", e -> {

                    final List<Coordinate> coordinates = createOuterBoundaries();

                    final PolygonFeature polygon = new PolygonFeature(
                            coordinates);

                    map.getFeatureLayer().addFeature(polygon);

                    map.addFeatureClickListener(event -> {
                        if (event
                                .getFeature() instanceof PolygonFeature feature) {
                            System.out.println(Arrays
                                    .deepToString(feature.getCoordinates()));
                        }
                    });

                });
        addSimplePolygonFeature.setId("add-simple-polygon-feature");

        //
        // ----
        NativeButton addPolygonWithHoleFeature = new NativeButton(
                "Add polygon feature with a hole", e -> {

                    final List<Coordinate> outerBoundary = createOuterBoundaries();
                    final List<Coordinate> hole = List.of(new Coordinate(6, 48), // SW
                            new Coordinate(6, 54), // NW
                            new Coordinate(14, 54), // NE
                            new Coordinate(14, 48), // SE
                            new Coordinate(6, 48) // SW
                    );

                    final Coordinate[][] coordinates = new Coordinate[][] {
                            outerBoundary.toArray(new Coordinate[0]),
                            hole.toArray(new Coordinate[0]) };
                    final PolygonFeature polygon = new PolygonFeature();
                    polygon.setCoordinates(coordinates);

                    map.getFeatureLayer().addFeature(polygon);

                    map.addFeatureClickListener(event -> {
                        if (event
                                .getFeature() instanceof PolygonFeature feature) {
                            System.out.println(Arrays
                                    .deepToString(feature.getCoordinates()));
                        }
                    });

                });
        addSimplePolygonFeature.setId("add-polygon-with-hole-feature");

        //
        // ----
        NativeButton updatePolygonCoordinates = new NativeButton(
                "Activate Drag & Drop", e -> map.getFeatureLayer().getFeatures()
                        .forEach(feature -> feature.setDraggable(true)));
        updatePolygonCoordinates.setId("activate-drag-drop");

        //
        // ----
        NativeButton updatePolygonStyle = new NativeButton(
                "Update polygon style", e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        final Feature feature = map.getFeatureLayer()
                                .getFeatures().get(0);
                        final Style style = new Style();
                        style.setStroke(new Stroke("red", 3));
                        style.setFill(new Fill("rgba(255, 0, 0, 0.1)"));
                        feature.setStyle(style);
                    }
                });
        updatePolygonStyle.setId("update-polygon-style");

        //
        // ----
        NativeButton deletePolygonsCoordinates = new NativeButton(
                "Delete polygons",
                e -> map.getFeatureLayer().removeAllFeatures());
        deletePolygonsCoordinates.setId("delete-polygons");

        add(map);
        add(new Div(addSimplePolygonFeature, addPolygonWithHoleFeature,
                updatePolygonCoordinates, updatePolygonStyle,
                deletePolygonsCoordinates));
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
