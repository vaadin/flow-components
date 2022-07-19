package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/change-user-projection")
public class ChangeUserProjectionPage extends Div {
    public ChangeUserProjectionPage() {
        Map.setUserProjection("EPSG:3857");

        Map map = new Map();
        // Set ID for easier referencing from tests
        map.getBackgroundLayer().setId("background-layer");
        // Coordinates for Turku in EPSG:3857
        map.setCenter(new Coordinate(2482424.644689998, 8500614.173537256));
        map.setZoom(10);

        Div eventData = new Div();
        eventData.setId("event-data");
        map.addViewMoveEndEventListener(event -> {
            String eventText = event.getCenter().getX() + ";"
                    + event.getCenter().getY() + ";";

            eventData.setText(eventText);
        });

        add(map, eventData);
    }
}
