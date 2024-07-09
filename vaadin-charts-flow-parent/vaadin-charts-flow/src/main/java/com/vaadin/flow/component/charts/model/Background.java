/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * An object, or array of objects, for backgrounds. Sub options include
 * backgroundColor (which can be solid or gradient), innerWidth, outerWidth,
 * borderWidth, borderColor.
 * <p>
 * <b>These configuration options apply only to polar and angular gauges trough
 * the Pane-configuration object.</b>
 */
public class Background extends AbstractConfigurationObject {
    private String outerRadius;
    private String innerRadius;
    private BackgroundShape shape;

    /**
     * Sets the outer radius of the circular shaped background using a string
     * representation of a percentage, e.g. "110%". The percentage is relative
     * to the radius of the chart.
     *
     * <em>This is applicable only to circular {@link ChartType#GAUGE} type
     * charts.</em>
     *
     * @param outerRadius
     */
    public void setOuterRadius(String outerRadius) {
        this.outerRadius = outerRadius;
    }

    /**
     * @see #setOuterRadius(String)
     */
    public String getOuterRadius() {
        return outerRadius;
    }

    /**
     * Sets the inner radius of the circular shaped background using a string
     * representation of a percentage, e.g. "110%". The percentage is relative
     * to the radius of the chart.
     *
     * <em>This is applicable only to circular {@link ChartType#GAUGE} type
     * charts.</em>
     *
     * @param innerRadius
     */
    public void setInnerRadius(String innerRadius) {
        this.innerRadius = innerRadius;
    }

    /**
     * @see #setInnerRadius(String)
     */
    public String getInnerRadius() {
        return innerRadius;
    }

    /**
     * Returns current shape of the background.
     *
     * @return Current shape.
     */
    public BackgroundShape getShape() {
        return shape;
    }

    /**
     * Sets the current shape of the background.
     *
     * @param shape
     *            New shape.
     */
    public void setShape(BackgroundShape shape) {
        this.shape = shape;
    }

}
