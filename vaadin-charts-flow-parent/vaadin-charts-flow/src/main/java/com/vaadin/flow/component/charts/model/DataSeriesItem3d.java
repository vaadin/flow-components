/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * DataSeriesItem that can hold also Z value. Used in e.g. bubble charts.
 */
public class DataSeriesItem3d extends DataSeriesItem {

    private Number z;

    public DataSeriesItem3d() {
        super();
    }

    /**
     * Constructs an item with X, Y and Z values
     *
     * @param x
     * @param y
     * @param z
     */
    public DataSeriesItem3d(Number x, Number y, Number z) {
        super(x, y);
        setZ(z);
    }

    /**
     * Sets the z value of the point.
     *
     * @param z
     */
    public void setZ(Number z) {
        this.z = z;
        makeCustomized();
    }

    /**
     * @return the z value
     */
    public Number getZ() {
        return z;
    }

}
