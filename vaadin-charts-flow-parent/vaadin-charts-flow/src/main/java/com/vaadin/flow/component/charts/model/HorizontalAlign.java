package com.vaadin.flow.component.charts.model;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Alignment of the title relative to the axis values and more generically
 * horizontal alignment. Possible values are RIGHT("right"), CENTER("center") or
 * LEFT("left")
 */
public enum HorizontalAlign implements ChartEnum {
    RIGHT("right"), CENTER("center"), LEFT("left");

    private final String align;

    private HorizontalAlign(String align) {
        this.align = align;
    }

    @Override
    public String toString() {
        return align;
    }
}
