package com.vaadin.flow.component.charts.model.style;

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
