package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
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
