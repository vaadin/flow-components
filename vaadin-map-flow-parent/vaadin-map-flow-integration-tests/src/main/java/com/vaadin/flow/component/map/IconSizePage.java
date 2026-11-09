/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.util.function.Consumer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/icon-size")
public class IconSizePage extends Div {
    public IconSizePage() {
        Map map = new Map();
        map.getView().setCenter(new Coordinate(0, 0));
        map.getView().setZoom(3);

        // Pin image has a natural size of 80x104
        addMarker(map, "width-and-height", "Width + height",
                new Coordinate(-20, 0), options -> {
                    options.setWidth(40);
                    options.setHeight(40);
                });
        addMarker(map, "width-only", "Width", new Coordinate(0, 0),
                options -> options.setWidth(40));
        addMarker(map, "height-only", "Height", new Coordinate(20, 0),
                options -> options.setHeight(52));
        Icon scaledIcon = addMarker(map, "scale", "Scale",
                new Coordinate(40, 0), options -> options.setScale(0.5));

        NativeButton changeScale = new NativeButton("Change scale",
                e -> scaledIcon.setScale(0.25));
        changeScale.setId("change-scale");

        add(map, changeScale);
    }

    private static Icon addMarker(Map map, String id, String label,
            Coordinate coordinate, Consumer<Icon.Options> configurer) {
        Icon.Options options = new Icon.Options();
        options.setImg(Assets.PIN.getHandler());
        configurer.accept(options);
        Icon icon = new Icon(options);
        MarkerFeature marker = new MarkerFeature(coordinate, icon);
        marker.setId(id);
        marker.setText(label);
        map.getFeatureLayer().addFeature(marker);
        return icon;
    }
}
