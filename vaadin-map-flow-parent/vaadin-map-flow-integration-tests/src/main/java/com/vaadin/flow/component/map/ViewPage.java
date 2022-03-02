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
            map.getView().setCenter(
                    new Coordinate(2482424.644689998, 8500614.173537256));
        });
        setCenterButton.setId("set-center-button");

        NativeButton setZoom = new NativeButton("Set Zoom", e -> {
            map.getView().setCenter(
                    new Coordinate(2482424.644689998, 8500614.173537256));
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
