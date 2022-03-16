package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/detach-attach")
public class DetachAttachPage extends Div {
    public DetachAttachPage() {
        Map map = new Map();
        add(map);
        // Modify viewport, so we can test some non-default values
        map.getView().setCenter(
                new Coordinate(2482424.644689998, 8500614.173537256));
        map.getView().setZoom(14);

        Div newContainer = new Div();

        NativeButton detachMap = new NativeButton("Detach", e -> {
            remove(map);
        });
        detachMap.setId("detach-map");

        NativeButton attachMap = new NativeButton("Attach", e -> {
            newContainer.add(map);
        });
        attachMap.setId("attach-map");

        NativeButton moveMap = new NativeButton("Move to different container",
                e -> {
                    remove(map);
                    newContainer.add(map);
                });
        moveMap.setId("move-map");

        add(newContainer, new Div(detachMap, attachMap, moveMap));
    }
}
