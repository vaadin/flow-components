/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Alignment of the title relative to the axis values and more generically
 * vertical alignment.
 */
public enum VerticalAlign implements ChartEnum {
    BOTTOM("bottom"), LOW("low"), MIDDLE("middle"), HIGH("high"), TOP("top");

    private final String align;

    private VerticalAlign(String align) {
        this.align = align;
    }

    @Override
    public String toString() {
        return align;
    }
}
