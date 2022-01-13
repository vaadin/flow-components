package com.vaadin.flow.component.map.configuration.layer;


import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public abstract class Layer extends AbstractConfigurationObject {
    private float opacity = 1;
    private boolean visible = true;

    @Override
    public String getType() {
        return Constants.OL_LAYER_LAYER;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
