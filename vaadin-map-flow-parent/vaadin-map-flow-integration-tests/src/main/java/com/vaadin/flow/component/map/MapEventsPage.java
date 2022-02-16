package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;

@Route("vaadin-map/map-events")
public class MapEventsPage extends Div {
    public MapEventsPage() {
        Div viewState = new Div();
        viewState.setId("view-state");

        Div eventData = new Div();
        eventData.setId("event-data");
        eventData.getStyle().set("white-space", "pre"); // Allow storing
                                                        // newlines in div

        Map map = new Map();
        map.setHeight("400px");
        map.setWidth("400px");
        map.getView().setZoom(3);

        // Add several overlapping markers to test whether map click event
        // contains feature event details
        int numOverlappingMarkers = 3;
        for (int i = 0; i < numOverlappingMarkers; i++) {
            MarkerFeature overlappingMarker = new MarkerFeature(
                    new Coordinate(2000000, 0));
            overlappingMarker.setId("overlapping-marker-feature-" + (i + 1));
            map.getFeatureLayer().addFeature(overlappingMarker);
        }

        NativeButton addMoveEndListener = new NativeButton(
                "Add move end listener", e -> {
                    map.addViewMoveEndEventListener(event -> {
                        View mapView = map.getView();
                        String stateText = mapView.getCenter().getX() + ";"
                                + mapView.getCenter().getY() + ";";
                        stateText += mapView.getRotation() + ";";
                        stateText += mapView.getZoom();

                        viewState.setText(stateText);

                        String eventDataText = event.getCenter().getX() + ";"
                                + event.getCenter().getY() + ";"
                                + event.getRotation() + ";" + event.getZoom();

                        eventData.setText(eventDataText);
                    });
                });
        addMoveEndListener.setId("add-move-end-listener");

        NativeButton addClickListener = new NativeButton("Add click listener",
                e -> {
                    map.addClickEventListener(event -> {
                        String coordinatesInfo = event.getCoordinate().getX()
                                + ";" + event.getCoordinate().getY();
                        String pixelPositionInfo = event.getMouseDetails()
                                .getAbsoluteX() + ";"
                                + event.getMouseDetails().getAbsoluteY();
                        String featureIds = event.getFeatures().stream()
                                .map(details -> details.getFeature().getId())
                                .collect(Collectors.joining(";"));
                        String eventDataText = String.join(
                                System.lineSeparator(), List.of(coordinatesInfo,
                                        pixelPositionInfo, featureIds));
                        eventData.setText(eventDataText);
                    });
                });
        addClickListener.setId("add-click-listener");

        add(map, viewState, eventData,
                new Div(addMoveEndListener, addClickListener));
    }
}
