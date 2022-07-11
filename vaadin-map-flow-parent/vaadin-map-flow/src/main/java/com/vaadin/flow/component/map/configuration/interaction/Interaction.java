package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

/**
 * Abstract base class for all interactions
 */
public abstract class Interaction extends AbstractConfigurationObject {

    private boolean active;

    public Interaction() {
        this.active = true;
    }

    public Interaction(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        markAsDirty();
    }

}
