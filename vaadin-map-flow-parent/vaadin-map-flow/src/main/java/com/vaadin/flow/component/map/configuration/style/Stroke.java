/**
 * Copyright 2000-2022 Vaadin Ltd.
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
    private float width;

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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        markAsDirty();
    }
}
