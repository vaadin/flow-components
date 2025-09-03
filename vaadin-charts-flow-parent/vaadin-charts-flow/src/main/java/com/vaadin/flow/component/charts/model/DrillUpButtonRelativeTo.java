/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * What box to align the button to.
 */
@Deprecated(since = "25.0", forRemoval = true)
public enum DrillUpButtonRelativeTo implements ChartEnum {

    PLOTBOX("plotBox"), SPACINGBOX("spacingBox");

    DrillUpButtonRelativeTo(String box) {
        this.box = box;
    }

    private String box;

    @Override
    public String toString() {
        return box;
    }
}
