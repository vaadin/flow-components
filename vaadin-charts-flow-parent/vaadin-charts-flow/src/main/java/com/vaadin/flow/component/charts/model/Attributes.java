/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.component.charts.model.style.Color;

/**
 * A collection of style attributes for the {@link Halo}
 */
public class Attributes extends AbstractConfigurationObject {

    private Color fill;
    private Color stroke;
    @JsonProperty("stroke-width")
    private Number strokeWidth;

    /**
     * @see #setFill(Color)
     * @return the fill
     */
    public Color getFill() {
        return fill;
    }

    /**
     * SVG fill attribute
     *
     * @param fill
     *            the fill
     */
    public void setFill(Color fill) {
        this.fill = fill;
    }

    /**
     * @see #setStroke(Color)
     * @return the stroke
     */
    public Color getStroke() {
        return stroke;
    }

    /**
     * SVG stroke attribute
     *
     * @param stroke
     *            the stroke
     */
    public void setStroke(Color stroke) {
        this.stroke = stroke;
    }

    /**
     * @see #setStrokeWidth(Number)
     * @return the stroke width
     */
    public Number getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * SVG stroke-width attribute
     *
     * @param strokeWidth
     *            the stroke width
     */
    public void setStrokeWidth(Number strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

}
