/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * The top of the frame around a 3D chart.
 */
public class Top extends AbstractConfigurationObject {

    private Color color;
    private Number size;

    public Top() {
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * The color of the panel.
     * <p>
     * Defaults to: transparent
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setSize(Number)
     */
    public Number getSize() {
        return size;
    }

    /**
     * The pixel thickness of the panel.
     * <p>
     * Defaults to: 1
     */
    public void setSize(Number size) {
        this.size = size;
    }
}
