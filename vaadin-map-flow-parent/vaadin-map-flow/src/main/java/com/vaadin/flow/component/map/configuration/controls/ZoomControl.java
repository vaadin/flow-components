/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * A control that provides zooming functionality on the map.
 */
public class ZoomControl extends Control {
    @Override
    public String getType() {
        return Constants.OL_CONTROL_ZOOM;
    }
}
