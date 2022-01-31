package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/map-events")
public class MapEventsPage extends Div {
    public MapEventsPage() {
        Div viewState = new Div();
        viewState.setId("view-state");

        Div eventData = new Div();
        eventData.setId("event-data");

        Map map = new Map();
        map.setHeight("400px");
        map.setWidth("400px");
        map.getView().setZoom(3);

        map.addViewMoveEndEventListener(event -> {
            View mapView = map.getView();
            String stateText = mapView.getCenter().getX() + ";"
                    + mapView.getCenter().getY() + ";";
            stateText += mapView.getRotation() + ";";
            stateText += mapView.getZoom();

            viewState.setText(stateText);

            String eventDataText = event.getCenter().getX() + ";"
                    + event.getCenter().getY() + ";" + event.getRotation() + ";"
                    + event.getZoom();

            eventData.setText(eventDataText);
        });

        map.addMapClickEventListener(event -> {
            eventData.setText(event.getCoordinate().getX() + ";" + event.getCoordinate().getY());
            viewState.setText(event.getMouseDetails().getAbsoluteX() + ";" + event.getMouseDetails().getAbsoluteY());
        });

        add(map, viewState, eventData);
    }
}
