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

/**
 * Set options related with look and position of targets.
 */
public class TargetOptions extends AbstractConfigurationObject {

    private Color borderColor;
    private Number borderWidth;
    private Color color;
    private String height;
    private String width;

    public TargetOptions() {
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Can set a <code>borderColor</code> of the rectangle representing the
     * target. When not set, the point's border color is used.
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
     * Can set the borderWidth of the rectangle representing the target.
     * <p>
     * Defaults to: 0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Can set a color of the rectangle representing the target. When not set,
     * point's color (if set in point's options - color) or zone of the target
     * value (if zones or negativeColor are set) or the same color as the point
     * has is used.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setHeight(String)
     */
    public String getHeight() {
        return height;
    }

    /**
     * The height of the rectangle representing the target.
     * <p>
     * Defaults to: 3
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * @see #setWidth(String)
     */
    public String getWidth() {
        return width;
    }

    /**
     * The width of the rectangle representing the target. Could be set as a
     * pixel value or as a percentage of a column width.
     * <p>
     * Defaults to: 140%
     */
    public void setWidth(String width) {
        this.width = width;
    }

}
