/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

/**
 * Base class for map controls, such as zoom and attribution controls.
 */
public abstract class Control extends AbstractConfigurationObject {
    private boolean visible = false;

    /**
     * Returns whether the control is visible.
     *
     * @return true if the control is visible, false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether the control is visible.
     *
     * @param visible
     *            true to make the control visible, false to hide it
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        markAsDirty();
        // Fire separate property change event for visibility change to allow
        // configuration to update the list of visible controls
        propertyChangeSupport.firePropertyChange("visible", null, null);
    }
}
