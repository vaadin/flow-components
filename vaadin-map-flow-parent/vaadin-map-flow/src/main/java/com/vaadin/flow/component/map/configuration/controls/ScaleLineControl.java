/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * A control that displays a scale line on the map.
 */
public class ScaleLineControl extends Control {
    @Override
    public String getType() {
        return Constants.OL_CONTROL_SCALE_LINE;
    }
}
