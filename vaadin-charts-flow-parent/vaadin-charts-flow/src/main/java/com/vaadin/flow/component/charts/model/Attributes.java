/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
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
     * @return
     */
    public Color getFill() {
        return fill;
    }

    /**
     * SVG fill attribute
     *
     * @param fill
     */
    public void setFill(Color fill) {
        this.fill = fill;
    }

    /**
     * @see #setStroke(Color)
     * @return
     */
    public Color getStroke() {
        return stroke;
    }

    /**
     * SVG stroke attribute
     *
     * @param stroke
     */
    public void setStroke(Color stroke) {
        this.stroke = stroke;
    }

    /**
     * @see #setStrokeWidth(Number)
     * @return
     */
    public Number getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * SVG stroke-width attribute
     *
     * @param strokeWidth
     */
    public void setStrokeWidth(Number strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

}
