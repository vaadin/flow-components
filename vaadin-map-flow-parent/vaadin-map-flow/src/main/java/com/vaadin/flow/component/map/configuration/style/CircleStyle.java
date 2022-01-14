package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.configuration.Constants;

public class CircleStyle extends RegularShape {

    private float radius;

    @Override
    public String getType() {
        return Constants.OL_STYLE_CIRCLE;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        notifyChange();
    }
}
