package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
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
                        eventData.setText(event.getCoordinate().getX() + ";"
                                + event.getCoordinate().getY() + ";"
                                + event.getMouseDetails().getAbsoluteX() + ";"
                                + event.getMouseDetails().getAbsoluteY());
                    });
                });
        addClickListener.setId("add-click-listener");

        add(map, viewState, eventData,
                new Div(addMoveEndListener, addClickListener));
    }
}
