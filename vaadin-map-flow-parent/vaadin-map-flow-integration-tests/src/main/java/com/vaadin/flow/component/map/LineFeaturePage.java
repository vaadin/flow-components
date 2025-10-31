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
import com.vaadin.flow.component.map.configuration.feature.LineFeature;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/line-feature")
public class LineFeaturePage extends Div {
    public LineFeaturePage() {
        Map map = new Map();

        LineFeature lineFeature = new LineFeature(new Coordinate(-10, 10),
                new Coordinate(10, 10), new Coordinate(-10, -10),
                new Coordinate(10, -10));

        map.getFeatureLayer().addFeature(lineFeature);
        add(map);

        NativeButton updateCoordinates = new NativeButton("Update coordinates",
                event -> lineFeature.setCoordinates(List.of(
                        new Coordinate(-10, 10), new Coordinate(10, 10),
                        new Coordinate(-10, -10), new Coordinate(10, -10),
                        new Coordinate(0, 0))));
        updateCoordinates.setId("update-coordinates");
        add(updateCoordinates);

        NativeButton updateStyle = new NativeButton("Update style", e -> {
            Style style = new Style();
            style.setStroke(new Stroke("red", 3));
            lineFeature.setStyle(style);
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
