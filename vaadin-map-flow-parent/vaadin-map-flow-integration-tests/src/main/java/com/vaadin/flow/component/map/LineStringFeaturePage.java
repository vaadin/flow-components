/**
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.map.configuration.feature.LineStringFeature;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/line-string-feature")
public class LineStringFeaturePage extends Div {
    public LineStringFeaturePage() {
        Map map = new Map();

        LineStringFeature lineStringFeature = new LineStringFeature(
                new Coordinate(-10, 10), new Coordinate(10, 10),
                new Coordinate(-10, -10), new Coordinate(10, -10));

        map.getFeatureLayer().addFeature(lineStringFeature);
        add(map);

        NativeButton updateCoordinates = new NativeButton("Update coordinates",
                event -> lineStringFeature.setCoordinates(List.of(
                        new Coordinate(-10, 10), new Coordinate(10, 10),
                        new Coordinate(-10, -10), new Coordinate(10, -10),
                        new Coordinate(0, 0))));
        updateCoordinates.setId("update-coordinates");
        add(updateCoordinates);

        NativeButton updateStyle = new NativeButton("Update style", e -> {
            Style style = new Style();
            style.setStroke(new Stroke("red", 3));
            lineStringFeature.setStyle(style);
        });
        updateStyle.setId("update-style");
        add(updateStyle);

        // Just used for manual testing
        NativeButton activateDragAndDrop = new NativeButton(
                "Activate Drag & Drop", e -> map.getFeatureLayer().getFeatures()
                        .forEach(feature -> feature.setDraggable(true)));
        add(activateDragAndDrop);
    }
}
