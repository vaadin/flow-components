/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Link styling options for Organization charts.
 */
public class Link extends AbstractConfigurationObject {
    private Color color;
    private Number lineWidth;
    private Number radius;
    private LinkType type;

    /**
     * @return the color of the link between nodes.
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the link between nodes.
     * 
     * @param color
     *            the color to use for the link
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @return the line width of the link in pixels.
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the line width of the link connecting nodes, in pixels.
     * 
     * @param lineWidth
     *            the width of the link line
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @return the radius for the rounded corners of the link.
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * Radius for the rounded corners of the links between nodes. Works for
     * {@link LinkType#DEFAULT} link type.
     * 
     * @param radius
     *            the radius for link corners
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    /**
     * @return the type of link shape.
     * @see #setType(LinkType)
     */
    public LinkType getType() {
        return type;
    }

    /**
     * Sets the type of link shape.
     * 
     * @param type
     *            the link shape type
     */
    public void setType(LinkType type) {
        this.type = type;
    }

}
