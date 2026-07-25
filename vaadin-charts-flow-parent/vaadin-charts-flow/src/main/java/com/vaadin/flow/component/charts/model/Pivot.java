/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * <p>
 * Options for the pivot or the center point of the gauge.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the pivot is styled with the
 * <code>.highcharts-gauge-series .highcharts-pivot</code> rule.
 * </p>
 * 
 * @since 6.0.1
 */
public class Pivot extends AbstractConfigurationObject {

    private Color backgroundColor;
    private Color borderColor;
    private Number borderWidth;
    private Number radius;

    public Pivot() {
    }

    /**
     * @see #setBackgroundColor(Color)
     * @since 18.0
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color or fill of the pivot.
     * <p>
     * Defaults to: #000000
     * 
     * @since 18.0
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setBorderColor(Color)
     * @since 18.0
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * The border or stroke color of the pivot. In able to change this, the
     * borderWidth must also be set to something other than the default 0.
     * <p>
     * Defaults to: #cccccc
     * 
     * @since 18.0
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderWidth(Number)
     * @since 18.0
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * The border or stroke width of the pivot.
     * <p>
     * Defaults to: 0
     * 
     * @since 18.0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * The pixel radius of the pivot.
     * <p>
     * Defaults to: 5
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }
}
