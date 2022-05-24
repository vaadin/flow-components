package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
