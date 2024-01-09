/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * What box to align the button to.
 */
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
