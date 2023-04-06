/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * <p>
 * Options for the handles for dragging the zoomed area.
 * </p>
 * 
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the navigator handles are styled with the
 * <code>.highcharts-navigator-handle</code>,
 * <code>.highcharts-navigator-handle-left</code> and
 * <code>.highcharts-navigator-handle-right</code> classes.
 * </p>
 */
public class Handles extends AbstractConfigurationObject {

    private Color backgroundColor;
    private Color borderColor;

    public Handles() {
    }

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The fill for the handle.
     * <p>
     * Defaults to: #f2f2f2
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * The stroke for the handle border and the stripes inside.
     * <p>
     * Defaults to: #999999
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
