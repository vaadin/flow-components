/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public class Stroke extends AbstractConfigurationObject {

    private String color;
    private double width;

    public Stroke() {
    }

    public Stroke(String color, double width) {
        this.color = color;
        this.width = width;
    }

    @Override
    public String getType() {
        return Constants.OL_STYLE_STROKE;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        markAsDirty();
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        markAsDirty();
    }
}
