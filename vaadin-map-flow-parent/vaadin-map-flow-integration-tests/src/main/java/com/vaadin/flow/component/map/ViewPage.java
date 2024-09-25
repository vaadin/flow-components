/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/view")
public class ViewPage extends Div {
    public ViewPage() {
        Map map = new Map();
        map.getView().setZoom(5);

        NativeButton setCenterButton = new NativeButton("Set Center", e -> {
            // here we set center
            map.getView().setCenter(new Coordinate(22.3, 60.45));
        });
        setCenterButton.setId("set-center-button");

        NativeButton setZoom = new NativeButton("Set Zoom", e -> {
            map.getView().setCenter(new Coordinate(22.3, 60.45));
            map.getView().setZoom(14);
        });
        setZoom.setId("set-zoom-button");

        NativeButton setRotation = new NativeButton("Set Rotation", e -> {
            // 45 degrees
            map.getView().setRotation(0.785398f);
        });
        setRotation.setId("set-rotation-button");

        add(map, setCenterButton, setZoom, setRotation);
    }
}
