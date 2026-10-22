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
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/icon-size")
public class IconSizePage extends Div {
    public IconSizePage() {
        Map map = new Map();

        // Pin image has a natural size of 80x104
        addMarker(map, options -> {
            options.setWidth(40);
            options.setHeight(40);
        });
        addMarker(map, options -> options.setWidth(40));
        addMarker(map, options -> options.setHeight(52));

        add(map);
    }

    private static void addMarker(Map map, Consumer<Icon.Options> configurer) {
        Icon.Options options = new Icon.Options();
        options.setImg(Assets.PIN.getHandler());
        configurer.accept(options);
        MarkerFeature marker = new MarkerFeature(new Coordinate(0, 0),
                new Icon(options));
        map.getFeatureLayer().addFeature(marker);
    }
}
