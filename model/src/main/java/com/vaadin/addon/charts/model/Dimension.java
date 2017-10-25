package com.vaadin.addon.charts.model;

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
 * Axial dimension.
 */
public enum Dimension implements ChartEnum {

    X("x"),
    Y("y"),
    XY("xy");

    private final String dimension;

    private Dimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return dimension;
    }
}
