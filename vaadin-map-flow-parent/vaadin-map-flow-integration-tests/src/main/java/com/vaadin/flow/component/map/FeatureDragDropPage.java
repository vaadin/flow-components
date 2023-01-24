package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-drag-drop")
public class FeatureDragDropPage extends Div {
    public FeatureDragDropPage() {
        Map map = new Map();

        MarkerFeature nairobi = new MarkerFeature(
                new Coordinate(36.818104, -1.302283));
        nairobi.setDraggable(true);
        nairobi.setId("nairobi-feature");

        map.getFeatureLayer().addFeature(nairobi);

        Span eventFeatureId = new Span();
        eventFeatureId.setId("event-feature-id");
        Span eventCoordinates = new Span();
        eventCoordinates.setId("event-coordinates");
        Span eventStartCoordinates = new Span();
        eventStartCoordinates.setId("event-start-coordinates");
        Span markerCoordinates = new Span();
        markerCoordinates.setId("marker-coordinates");

        map.addFeatureDragDropListener(event -> {
            eventFeatureId.setText(event.getFeature().getId());
            eventCoordinates.setText(formatCoordinates(event.getCoordinate()));
            eventStartCoordinates
                    .setText(formatCoordinates(event.getStartCoordinate()));
            markerCoordinates
                    .setText(formatCoordinates(nairobi.getCoordinates()));
        });

        add(map);
        add(new Div(new Span("Drag&Drop event - Feature ID: "),
                eventFeatureId));
        add(new Div(new Span("Drag&Drop event - Coordinates: "),
                eventCoordinates));
        add(new Div(new Span("Drag&Drop event - Start Coordinates: "),
                eventStartCoordinates));
        add(new Div(new Span("Marker Coordinates: "), markerCoordinates));
    }

    private static String formatCoordinates(Coordinate coordinate) {
        return String.format("%s;%s", coordinate.getX(), coordinate.getY());
    }
}
