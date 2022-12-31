/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

public class ButtonTheme extends AbstractConfigurationObject {
    private Color fill;
    private Color stroke;
    @JsonProperty("stroke-width")
    private Number strokeWidth;
    private Style style;
    @JsonInclude(Include.NON_DEFAULT)
    private Number width = 32;

    public Color getFill() {
        return fill;
    }

    public void setFill(Color fill) {
        this.fill = fill;
    }

    public Color getStroke() {
        return stroke;
    }

    public void setStroke(Color stroke) {
        this.stroke = stroke;
    }

    public Number getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Number strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }
}
