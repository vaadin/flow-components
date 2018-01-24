package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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
 * The shape of the pane background. When solid, the background is circular.
 * When arc, the background extends only from the min to the max of the value axis.
 *
 * Defaults to solid.
 */
public enum BackgroundShape implements ChartEnum {

    SOLID("solid"),
    ARC("arc");

    private final String shape;

    private BackgroundShape(String shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return shape;
    }
}
