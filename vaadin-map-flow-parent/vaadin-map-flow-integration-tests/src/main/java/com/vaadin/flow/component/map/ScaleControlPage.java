/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.controls.ScaleControl;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/scale-control")
public class ScaleControlPage extends Div {
    public ScaleControlPage() {
        Map map = new Map();
        map.setId("map");

        // Enable scale control by default
        ScaleControl scaleControl = map.getControls().getScale();
        scaleControl.setVisible(true);

        NativeButton toggleDisplayMode = new NativeButton("Toggle display mode",
                e -> {
                    if (scaleControl
                            .getDisplayMode() == ScaleControl.DisplayMode.LINE) {
                        scaleControl
                                .setDisplayMode(ScaleControl.DisplayMode.BAR);
                    } else {
                        scaleControl
                                .setDisplayMode(ScaleControl.DisplayMode.LINE);
                    }
                });
        toggleDisplayMode.setId("toggle-display-mode");

        NativeButton setImperialUnits = new NativeButton("Set imperial units",
                e -> {
                    scaleControl.setUnits(ScaleControl.Unit.IMPERIAL);
                });
        setImperialUnits.setId("set-imperial-units");

        NativeButton setTwoSteps = new NativeButton("Set two steps", e -> {
            scaleControl.setScaleBarSteps(2);
        });
        setTwoSteps.setId("set-two-steps");

        NativeButton toggleScaleBarText = new NativeButton(
                "Toggle scale bar text", e -> {
                    scaleControl.setScaleBarTextVisible(
                            !scaleControl.isScaleBarTextVisible());
                });
        toggleScaleBarText.setId("toggle-scale-bar-text");

        add(map, new Div(toggleDisplayMode, setImperialUnits, setTwoSteps,
                toggleScaleBarText));
    }
}
