package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import javax.lang.model.element.ModuleElement;

@Route("vaadin-map/map-events")
public class MapEventsPage extends Div {
    public MapEventsPage() {
        Map map = new Map();
        map.setHeight("400px");
        map.setWidthFull();

        map.addViewMoveEndEventListener(event -> {
            System.out.println("MapMoveEndEvent");
            System.out.println(event.getRotation());

            System.out.println(map.getView().getRotation());
        });

        add(map);
    }
}
