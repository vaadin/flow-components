package com.vaadin.flow.component.map.configuration.layer;


import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

public abstract class Layer extends AbstractConfigurationObject {
    private float opacity = 1;
    private boolean visible = true;

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity, boolean markDirty) {
        this.opacity = opacity;
        notifyChange();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        notifyChange();
    }
}
