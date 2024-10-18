/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

/**
 * Abstract base class for geometries
 */
public abstract class SimpleGeometry extends AbstractConfigurationObject {
    /**
     * Translate the geometry by the specified delta
     *
     * @param deltaX
     *            amount to move on x-axis
     * @param deltaY
     *            amount to move on y-axis
     */
    public abstract void translate(double deltaX, double deltaY);
}
