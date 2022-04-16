package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

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

/**
 * An object, or array of objects, for backgrounds. Sub options include
 * backgroundColor (which can be solid or gradient), innerWidth, outerWidth,
 * borderWidth, borderColor.
 * <p>
 * <b>These configuration options apply only to polar and angular gauges trough
 * the Pane-configuration object.</b>
 */
public class Background extends AbstractConfigurationObject {
    private Color backgroundColor;
    private Color borderColor;
    private Number borderWidth;
    private String className;
    private String outerRadius;
    private String innerRadius;
    private BackgroundShape shape;

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color
     *
     * @param backgroundColor
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
     * Sets the border color
     *
     * @param borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Sets the width of the border
     *
     * @param borderWidth
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * Returns the class name of the background
     *
     * @see #setClassName(String)
     * @return The class name of the background
     */
    public String getClassName() {
        return className;
    }

    /**
     * The class name for this background. Defaults to highcharts-pane.
     *
     * @param className
     *            new class name of the background
     */
    public void setClassName(String className) {
        this.className = className;
    }

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
