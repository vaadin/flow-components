package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.router.Route;

import javax.lang.model.element.ModuleElement;

@Route("vaadin-map/map-events")
public class MapEventsPage extends Div {
    public MapEventsPage() {
        Map map = new Map();
        map.setHeight("400px");
        map.setWidthFull();

        map.addViewMoveEndEventListener(event -> {
            View mapView = map.getView();

            System.out.println(mapView.getRotation());
            System.out.println(mapView.getZoom());
            System.out.println(mapView.getRotation());
            System.out.println(mapView.getRotation());
            System.out.println(mapView.getCenter().getX() + " " + mapView.getCenter().getY());
        });

        add(map);
    }
}
