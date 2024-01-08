/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

public class ColorAxis extends YAxis {

    private static final long serialVersionUID = 1L;
    private Color maxColor;
    private Color minColor;

    /**
     * @see #setMaxColor(Color)
     */
    public Color getMaxColor() {
        return maxColor;
    }

    /**
     * Solid gauge only. Unless <a href="#yAxis.stops">stops</a> are set, the
     * color to represent the maximum value of the Y axis.
     * <p>
     * Defaults to: #003399
     */
    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    /**
     * @see #setMinColor(Color)
     */
    public Color getMinColor() {
        return minColor;
    }

    /**
     * Solid gauge only. Unless <a href="#yAxis.stops">stops</a> are set, the
     * color to represent the minimum value of the Y axis.
     * <p>
     * Defaults to: #e6ebf5
     */
    public void setMinColor(Color minColor) {
        this.minColor = minColor;
    }

}
