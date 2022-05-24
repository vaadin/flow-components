package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
