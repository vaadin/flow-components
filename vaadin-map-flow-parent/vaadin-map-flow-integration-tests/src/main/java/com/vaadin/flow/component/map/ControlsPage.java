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
import com.vaadin.flow.component.map.configuration.controls.AttributionControl;
import com.vaadin.flow.component.map.configuration.controls.ScaleControl;
import com.vaadin.flow.component.map.configuration.controls.ZoomControl;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/controls")
public class ControlsPage extends Div {
    public ControlsPage() {
        Map map = new Map();
        add(map);

        NativeButton toggleAttributions = new NativeButton(
                "Toggle attributions", (e) -> {
                    AttributionControl control = map.getControls()
                            .getAttribution();
                    control.setVisible(!control.isVisible());
                });
        toggleAttributions.setId("toggle-attributions");

        NativeButton toggleZoom = new NativeButton("Toggle zoom", (e) -> {
            ZoomControl control = map.getControls().getZoom();
            control.setVisible(!control.isVisible());
        });
        toggleZoom.setId("toggle-zoom");

        NativeButton toggleScale = new NativeButton("Toggle scale", (e) -> {
            ScaleControl control = map.getControls().getScale();
            control.setVisible(!control.isVisible());
        });
        toggleScale.setId("toggle-scale");

        add(new Div(toggleAttributions, toggleZoom, toggleScale));
    }
}
