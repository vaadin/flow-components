package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public class Fill extends AbstractConfigurationObject {

    private String color;

    @Override
    public String getType() {
        return Constants.OL_STYLE_FILL;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        notifyChange();
    }
}
