/**
 * Copyright (C) 2000-2024 Vaadin Ltd
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
    @JsonProperty("stroke-width")
    private Number strokeWidth;
    @JsonInclude(Include.NON_DEFAULT)
    private Number width = 32;
    private Number r;

    public Number getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Number strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }

    public Number getR() {
        return r;
    }

    public void setR(Number r) {
        this.r = r;
    }
}
