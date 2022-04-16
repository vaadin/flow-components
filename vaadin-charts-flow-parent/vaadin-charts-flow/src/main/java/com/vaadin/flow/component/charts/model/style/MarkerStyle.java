package com.vaadin.flow.component.charts.model.style;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
