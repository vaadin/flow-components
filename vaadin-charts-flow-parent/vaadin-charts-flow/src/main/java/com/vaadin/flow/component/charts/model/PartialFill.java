/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * PartialFill configuration object to be used in {@link PlotOptionsXrange}.
 * Typically used to visualize how much of a task is performed.
 */
public class PartialFill extends AbstractConfigurationObject {

    private Color fill;

    /**
     * Creates an empty PartialFill configuration object
     */
    public PartialFill() {

    }

    /**
     * Creates a new PartialFill with the defined fill color
     *
     * @param fill
     *            color to be used for partial fills
     */
    public PartialFill(Color fill) {
        this.fill = fill;
    }

    /**
     * @see #setFill(Color)
     *
     * @return the color used for partial fills
     */
    public Color getFill() {
        return fill;
    }

    /**
     * The fill color to be used for partial fills. When <code>null</code>, a
     * darker shade of the point's color is used.
     *
     * @param fill
     *            color to be used for partial fills
     */
    public void setFill(Color fill) {
        this.fill = fill;
    }

}
