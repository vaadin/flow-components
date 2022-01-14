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
        notifyChange();
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        notifyChange();
    }
}
