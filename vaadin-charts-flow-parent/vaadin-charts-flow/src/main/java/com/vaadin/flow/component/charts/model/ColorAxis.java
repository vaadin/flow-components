package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
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
