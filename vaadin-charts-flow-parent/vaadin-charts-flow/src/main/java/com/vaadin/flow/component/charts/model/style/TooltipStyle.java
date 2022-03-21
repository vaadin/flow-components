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
 * Style options for tooltips
 */
public class TooltipStyle extends AbstractConfigurationObject {

    private Color backgroundColor;
    private Number borderWidth;
    private Number borderRadius;
    private Color borderColor;
    private Boolean followPointer = false;
    private Style style = new Style();

    /**
     * @return The background color of tooltips, null if not defined
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of tooltips
     *
     * @param backgroundColor
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @return The width of the border of tooltips, or null if not defined
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Sets the width of the border of tooltips
     *
     * @param borderWidth
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @return The style attributes for tooltips
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the style attributes for tooltips
     *
     * @param style
     *            Style attributes
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @return The border radius of tooltips
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * Sets the border radius of tooltips
     *
     * @param borderRadius
     *            the border radius in pixels
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @return the followPointer
     */
    public Boolean getFollowPointer() {
        return followPointer;
    }

    /**
     * @param followPointer
     *            the followPointer to set
     */
    public void setFollowPointer(Boolean followPointer) {
        this.followPointer = followPointer;
    }

    /**
     * @see #setBorderColor(Color)
     * @return The color of the plot border, null if not defined
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the color of the tooltip border. When null, the border takes the
     * color of the corresponding series or point. Defaults to null.
     *
     * @param borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
