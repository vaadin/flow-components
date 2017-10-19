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
 * Parameters for in what dimensions the user can zoom by dragging the mouse.
 */
public enum ZoomType implements ChartEnum {
    X("x"), Y("y"), XY("xy");

    private final String zoomType;

    private ZoomType(String zoomType) {
        this.zoomType = zoomType;
    }

    @Override
    public String toString() {
        return zoomType;
    }
}
