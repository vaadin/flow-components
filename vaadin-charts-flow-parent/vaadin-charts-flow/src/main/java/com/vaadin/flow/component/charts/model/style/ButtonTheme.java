package com.vaadin.flow.component.charts.model.style;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */


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
