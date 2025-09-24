/**
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/detach-attach")
public class DetachAttachPage extends Div {
    public DetachAttachPage() {
        Map map = new Map();
        add(map);

        // Add some custom configuration to test non-default values
        map.getView().setCenter(new Coordinate(22.3, 60.45));
        map.getView().setZoom(14);

        MarkerFeature marker = new MarkerFeature(new Coordinate(22.3, 60.45));
        Icon.Options options = new Icon.Options();
        options.setImg(Assets.POINT.getHandler());
        Icon icon = new Icon(options);
        marker.setIcon(icon);
        map.getFeatureLayer().addFeature(marker);

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
