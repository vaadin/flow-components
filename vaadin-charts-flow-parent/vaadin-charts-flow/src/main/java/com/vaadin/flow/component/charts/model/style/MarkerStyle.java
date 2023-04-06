/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

/**
 * Marker style
 */
public class MarkerStyle extends AbstractConfigurationObject {
    private Color lineColor;

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the color of the point marker's outline. When null, the series' or
     * point's color is used. Defaults to "#FFFFFF".
     * 
     * @param lineColor
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

}
